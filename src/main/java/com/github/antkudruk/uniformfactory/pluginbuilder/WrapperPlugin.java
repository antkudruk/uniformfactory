/*
    Copyright 2020 - 2021 Anton Kudruk

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.github.antkudruk.uniformfactory.pluginbuilder;

import com.github.antkudruk.uniformfactory.base.bytecode.InitFieldUsingClassInstanceMethodImplementation;
import com.github.antkudruk.uniformfactory.base.bytecode.InitFieldWithConstructorFieldUsingThisImplementation;
import com.github.antkudruk.uniformfactory.base.bytecode.InitFieldWithDefaultConstructorImplementation;
import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.AmbiguousGetWrapperMethodException;
import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.GetWrapperMethodNotExistsException;
import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.GetWrapperMethodWrongTypeException;
import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.OriginInterfaceNotDefinedException;
import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.StaticConstructorGeneratorException;
import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.SelectClassCriteriaNotDefinedException;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Gradle plugin.
 * Generates wrapper objects for each origin object the following way:
 * For each class marked with {@code typeMarker} annotation it instantiates
 * {@code classFactoryGenerator} class and calls
 * {@code MetaClassFactory::generateMetaClass} method on its instance to
 * get a {@code Function} that will be used to create a wrapper for each origin
 * object.
 *
 */
public class WrapperPlugin implements Plugin {

    private static final String INSTANCE_FIELD_NAME = "INSTANCE";

    private final Class originInterface;
    private final Predicate<TypeDescription> selectTypeCriteria;
    private final List<WrapperDescriptor<?>> wrappers;
    private final Map<String, DynamicType.Unloaded> classGeneratorSingletonContainer;

    public WrapperPlugin(
            Class originInterface,
            Predicate<TypeDescription> selectTypeCriteria,
            List<WrapperDescriptor<?>> wrappers
    ) {

        this.originInterface = originInterface;
        this.selectTypeCriteria = selectTypeCriteria;
        this.wrappers = wrappers;
        classGeneratorSingletonContainer = wrappers
                .stream()
                .collect(Collectors.toMap(
                        WrapperDescriptor::getFieldName,
                        e -> createSingletonHolder(e.getWrappers())));
    }

    /**
     * Creates the plugin to build wrappers.
     *
     * @param originInterface Interface to implement by the origin class
     * @param getWrapperMethodName Origin method to ter the wrapper
     * @param wrapperClass Interface to implement by wrappers
     * @param selectTypeCriteria Determines if the class has to be enhanced with the wrapper
     * @param wrapperFieldName Field name to store wrappers
     * @param classFactoryGeneratorFieldName Field name to store wrapper class generators
     * @param classFactoryGenerator Class factory generator class. You'll have it's singleton instance created.
     * @param <W> Wrapper type
     */
    public <W> WrapperPlugin(
            Class originInterface,
            String getWrapperMethodName,
            Class<W> wrapperClass,
            Predicate<TypeDescription> selectTypeCriteria,
            String wrapperFieldName,
            String classFactoryGeneratorFieldName,
            Class<? extends MetaClassFactory<W>> classFactoryGenerator) {

        this(
                originInterface,
                selectTypeCriteria,
                Collections.singletonList(new WrapperDescriptor<>(
                        getWrapperMethodName,
                        wrapperFieldName,
                        classFactoryGeneratorFieldName,
                        wrapperClass,
                        classFactoryGenerator
                ))
        );
    }

    /**
     * Creates the plugin to build wrappers.
     *
     * @param originInterface Interface to implement by the origin class
     * @param getWrapperMethodName Origin method to ter the wrapper
     * @param wrapperClass Interface to implement by wrappers
     * @param typeMarker Annotation to mark origin classes
     * @param wrapperFieldName Field name to store wrappers
     * @param classFactoryGeneratorFieldName Field name to store wrapper class generators
     * @param classFactoryGenerator Class factory generator class. You'll have it's singleton instance created.
     * @param <W> Wrapper type
     */
    public <W> WrapperPlugin(
            Class originInterface,
            String getWrapperMethodName,
            Class<W> wrapperClass,
            Class<? extends Annotation> typeMarker,
            String wrapperFieldName,
            String classFactoryGeneratorFieldName,
            Class<? extends MetaClassFactory<W>> classFactoryGenerator) {

        this(
                originInterface,
                getWrapperMethodName,
                wrapperClass,
                typeDefinition -> isApplicable(typeMarker, typeDefinition),
                wrapperFieldName,
                classFactoryGeneratorFieldName,
                classFactoryGenerator);
    }

    /**
     * Creates the plugin to build wrappers.
     *
     * @param originInterface Interface to implement by the origin class
     * @param wrapperClass Interface to implement by wrappers
     * @param typeMarker Annotation to mark origin classes
     * @param pluginName Name of the plugin
     * @param classFactoryGenerator Class factory generator class. You'll have it's singleton instance created.
     * @param <W> Wrapper type
     */
    public <W> WrapperPlugin(
            Class originInterface,
            Class<W> wrapperClass,
            Class<? extends Annotation> typeMarker,
            String pluginName,
            Class<? extends MetaClassFactory<W>> classFactoryGenerator) {

        this(
                originInterface,
                getSingleMethod(originInterface),
                wrapperClass,
                typeMarker,
                pluginName + "Wrapper",
                pluginName + "WrapperGenerator",
                classFactoryGenerator);
    }

    /**
     * Creates the plugin to build wrappers.
     *
     * @param originInterface Interface to implement by the origin class
     * @param wrapperClass Interface to implement by wrappers
     * @param selectTypeCriteria Determines if the class has to be enhanced with the wrapper
     * @param pluginName Name of the plugin
     * @param classFactoryGenerator Class factory generator class. You'll have it's singleton instance created.
     * @param <W> Wrapper type
     */
    public <W> WrapperPlugin(
            Class originInterface,
            Class<W> wrapperClass,
            Predicate<TypeDescription> selectTypeCriteria,
            String pluginName,
            Class<? extends MetaClassFactory<W>> classFactoryGenerator) {

        this(
                originInterface,
                getSingleMethod(originInterface),
                wrapperClass,
                selectTypeCriteria,
                pluginName + "Wrapper",
                pluginName + "WrapperGenerator",
                classFactoryGenerator);
    }

    /**
     * Creates the plugin to build wrappers.
     *
     * @param originInterface Interface to implement by the origin class
     * @param wrapperClass Interface to implement by wrappers
     * @param typeMarker Annotation to mark origin classes
     * @param wrapperFieldName Field name to store wrappers
     * @param classFactoryGeneratorFieldName Field name to store wrapper class generators
     * @param classFactoryGenerator Class factory generator class. You'll have it's singleton instance created.
     * @param <W> Wrapper type
     */
    public <W> WrapperPlugin(
            Class originInterface,
            Class<W> wrapperClass,
            Class<? extends Annotation> typeMarker,
            String wrapperFieldName,
            String classFactoryGeneratorFieldName,
            Class<? extends MetaClassFactory<W>> classFactoryGenerator) {

        this(
                originInterface,
                getSingleMethod(originInterface),
                wrapperClass,
                typeMarker,
                wrapperFieldName,
                classFactoryGeneratorFieldName,
                classFactoryGenerator);
    }

    /**
     * Creates the plugin to build wrappers.
     *
     * @param originInterface Interface to implement by the origin class
     * @param getWrapperMethodName Origin method to ter the wrapper
     * @param wrapperClass Interface to implement by wrappers
     * @param typeMarker Annotation to mark origin classes
     * @param pluginName Name of the plugin
     * @param classFactoryGenerator Class factory generator class. You'll have it's singleton instance created.
     * @param <W> Wrapper type
     */
    public <W> WrapperPlugin(
            Class originInterface,
            String getWrapperMethodName,
            Class<W> wrapperClass,
            Class<? extends Annotation> typeMarker,
            String pluginName,
            Class<? extends MetaClassFactory<W>> classFactoryGenerator) {

        this(
                originInterface,
                getWrapperMethodName,
                wrapperClass,
                typeMarker,
                pluginName + "Wrapper",
                pluginName + "WrapperGenerator",
                classFactoryGenerator);
    }

    // TODO: Builder does not process default parameters unless it's build method called. Make the constructor work .
    private <W> WrapperPlugin(Builder<W> builder) {
        this(
                builder.originInterface,
                builder.getWrapperMethodName,
                builder.wrapperClass,
                builder.selectClassCriteria,
                builder.wrapperFieldName,
                builder.wrapperClassFactoryFieldName,
                builder.classFactoryGenerator
        );
    }

    private static String checkFieldName(String pluginName) {
        if(pluginName.matches("[a-zA-Z0-9_]+")) {
            return pluginName;
        } else {
            throw new RuntimeException("Invalid field name " + pluginName + ": should match the following regular expression: [a-zA-Z0-9_]+");
        }
    }

    private static String getSingleMethod(Class<?> functionalClass) {
        if(functionalClass.getDeclaredMethods().length == 1) {
            return functionalClass.getDeclaredMethods()[0].getName();
        } else {
            throw new RuntimeException(functionalClass.getSimpleName() + " should contain one and only one method.");
        }
    }

    /**
     * Looks for the annotation not only in the current class, but in all implementing interfaces.
     *
     * @param typeMarker Annotation type to look for
     * @param typeDefinitions Type to look for the annotation in
     * @return
     */
    private static boolean isApplicable(
            Class<? extends Annotation> typeMarker,
            TypeDescription typeDefinitions) {
        return typeMarker.getAnnotation(Inherited.class) != null
                ? isApplicableRecursive(typeMarker, typeDefinitions)
                : typeDefinitions
                        .getDeclaredAnnotations()
                        .isAnnotationPresent(typeMarker);
    }

    private static boolean isApplicableRecursive(
            Class<? extends Annotation> typeMarker,
            TypeDescription typeDefinitions) {

        boolean thisTypeMatches = typeDefinitions
                .getDeclaredAnnotations()
                .isAnnotationPresent(typeMarker);

        return thisTypeMatches || typeDefinitions
                .getInterfaces()
                .stream()
                .anyMatch(iface -> isApplicableRecursive(typeMarker, iface.asErasure()));
    }

    /**
     * Called by ByteBuddy plugin to transform matching classes
     * @param builder Builder to transform the class
     * @param typeDescription Type description to analyse transformed class.
     * @return New version of the builder to create transformed class.
     */
    @Override
    public DynamicType.Builder<?> apply(
            DynamicType.Builder<?> builder,
            TypeDescription typeDescription,
            ClassFileLocator classFileLocator) {

        // Add interface if needed
        if (!typeDescription.getInterfaces().contains(new TypeDescription.ForLoadedType(originInterface))) {
            builder = builder.implement(originInterface);
        }


        for(WrapperDescriptor<?> wrapperDescriptor: wrappers) {
            // Add method implementation if needed
            MethodDescription originMethodReturningWrapper = new TypeDescription.ForLoadedType(originInterface)
                    .getDeclaredMethods()
                    .filter(ElementMatchers.named(wrapperDescriptor.getMethodName()))
                    .getOnly();

            String classFactoryGeneratorFieldName = wrapperDescriptor.getClassFactoryGeneratorFieldName();

            if (implementsMethod(typeDescription, originMethodReturningWrapper)) {

                builder = builder
                        .defineField(wrapperDescriptor.getFieldName(), wrapperDescriptor.getWrapperClass(), Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC)
                        .defineField(classFactoryGeneratorFieldName, Function.class, Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC)
                        .invokable(ElementMatchers.isTypeInitializer())
                        .intercept(new InitFieldUsingClassInstanceMethodImplementation(
                                classGeneratorSingletonContainer.get(wrapperDescriptor.getFieldName()).getTypeDescription(),
                                classFactoryGeneratorFieldName,
                                wrapperDescriptor.getWrappers()
                        ))
                        .invokable(ElementMatchers.isConstructor())
                        .intercept(SuperMethodCall.INSTANCE.andThen(
                                new InitFieldWithConstructorFieldUsingThisImplementation(
                                        classFactoryGeneratorFieldName, wrapperDescriptor.getFieldName())))
                        .define(new TypeDescription.ForLoadedType(originInterface)
                                .getDeclaredMethods()
                                .filter(ElementMatchers.named(wrapperDescriptor.getMethodName()))
                                .getOnly())
                        .intercept(FieldAccessor.ofField(wrapperDescriptor.getFieldName()));
            }
        }

        return builder.require(classGeneratorSingletonContainer.values().toArray(new DynamicType.Unloaded[0]));
    }

    /**
     * Checks if typeDescription implements the method originMethodReturningWrapper directly.
     * @param typeDescription Type to look for the method
     * @param originMethodReturningWrapper Method
     * @return True if the method is found, false otherwise.
     */
    private boolean implementsMethod (TypeDescription typeDescription, MethodDescription originMethodReturningWrapper) {

        TypeDescription[] argumentTypes = originMethodReturningWrapper
                .getParameters()
                .stream()
                .map(ParameterDescription::getType)
                .map(TypeDefinition::asErasure)
                .toArray(TypeDescription[]::new);

        return typeDescription.getDeclaredMethods()
                .filter(ElementMatchers.named(originMethodReturningWrapper.getName()))
                .filter(ElementMatchers.takesArguments(argumentTypes))
                .isEmpty();
    }

    private DynamicType.Unloaded createSingletonHolder(Class<? extends MetaClassFactory<?>> classFactoryGenerator) {
        ByteBuddy byteBuddy = new ByteBuddy();
        return byteBuddy
                .subclass(Object.class, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                .defineField(INSTANCE_FIELD_NAME, classFactoryGenerator, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
                .invokable(ElementMatchers.isTypeInitializer())
                .intercept(new InitFieldWithDefaultConstructorImplementation(
                        new TypeDescription.ForLoadedType(classFactoryGenerator),
                        INSTANCE_FIELD_NAME))
                .make();
    }

    @Override
    public void close() {

    }

    @Override
    public boolean matches(TypeDescription target) {
        return !target.isInterface() && !target.isAnnotation() && selectTypeCriteria.test(target);
    }

    public static class Builder<W> {
        private final Class<W> wrapperClass;
        private Class originInterface;
        private String getWrapperMethodName = null;
        private Predicate<TypeDescription> selectClassCriteria;
        private String wrapperFieldName = "wrapper";
        private String wrapperClassFactoryFieldName = "wrapperClassFactory";
        private Class<? extends MetaClassFactory<W>> classFactoryGenerator;

        public Builder(Class<W> wrapperClass) {
            this.wrapperClass = wrapperClass;
        }

        public Builder<W> setOriginInterface(Class originInterface) {
            this.originInterface = originInterface;
            return this;
        }

        public Builder<W> setGetWrapperMethodName(String getWrapperMethodName) {
            this.getWrapperMethodName = getWrapperMethodName;
            return this;
        }

        public Builder<W> setTypeMarker(Class<? extends Annotation> typeMarker) {
            this.selectClassCriteria = (td) -> td.getDeclaredAnnotations().isAnnotationPresent(typeMarker);
            return this;
        }

        public Builder<W> setSelectClassCriteria(Predicate<TypeDescription> selectClassCriteria) {
            this.selectClassCriteria = selectClassCriteria;
            return this;
        }

        public Builder<W> setWrapperFieldName(String wrapperFieldName) {
            this.wrapperFieldName = wrapperFieldName;
            return this;
        }

        public Builder<W> setWrapperClassFactoryFieldName(String wrapperClassFactoryFieldName) {
            this.wrapperClassFactoryFieldName = wrapperClassFactoryFieldName;
            return this;
        }

        public Builder<W> setClassFactoryGenerator(Class<? extends MetaClassFactory<W>> staticConstructorGenerator) {
            this.classFactoryGenerator = staticConstructorGenerator;
            return this;
        }

        public WrapperPlugin build() {

            if (originInterface == null) {
                throw new OriginInterfaceNotDefinedException();
            }

            if (selectClassCriteria == null) {
                throw new SelectClassCriteriaNotDefinedException();
            }

            if (classFactoryGenerator == null) {
                throw new StaticConstructorGeneratorException(
                        "You should specify static method returning a constructor "
                                + "for your wrapper class according to the origin object type"
                );
            }

            if (getWrapperMethodName == null) {
                if (originInterface.getDeclaredMethods().length != 1) {
                    throw new AmbiguousGetWrapperMethodException();
                } else {
                    getWrapperMethodName = originInterface.getDeclaredMethods()[0].getName();
                }
            }

            Method getWrapperMethod;
            try {
                //noinspection unchecked
                getWrapperMethod = originInterface.getMethod(getWrapperMethodName);
            } catch (NoSuchMethodException ignore) {
                throw new GetWrapperMethodNotExistsException(
                        getWrapperMethodName, originInterface);
            }
            if (!getWrapperMethod.getReturnType().equals(wrapperClass)) {
                throw new GetWrapperMethodWrongTypeException(
                        getWrapperMethodName, originInterface, wrapperClass);
            }

            return new WrapperPlugin(
                    originInterface,
                    getWrapperMethodName,
                    wrapperClass,
                    selectClassCriteria,
                    wrapperFieldName,
                    wrapperClassFactoryFieldName,
                    classFactoryGenerator
            );
        }
    }
}
