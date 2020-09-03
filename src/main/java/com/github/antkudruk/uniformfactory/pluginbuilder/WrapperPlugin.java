/*
    Copyright 2020 Anton Kudruk

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
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Gradle plugin.
 * Generates wrapper objects for each origin object the following way:
 * For each class marked with {@code typeMarker} annotation it instantiates
 * {@code classFactoryGenerator} class and calls
 * {@code MetaClassFactory::generateMetaClass} method on its instance to
 * get a {@code Function} that will be used to create a wrapper for each origin
 * object.
 *
 * @param <W> Wrapper class
 */
public class WrapperPlugin<W> implements Plugin {

    private static final String INSTANCE_FIELD_NAME = "INSTANCE";

    private final Class originInterface;
    private final String getWrapperMethodName;
    private final Class<W> wrapperClass;
    // TODO: Replace with lambda private final Class<? extends Annotation> typeMarker;
    private final Predicate<TypeDescription> selectTypeCriteria;
    private final String wrapperFieldName;
    private final String classFactoryGeneratorFieldName;
    private final Class<? extends MetaClassFactory<W>> classFactoryGenerator;

    private final DynamicType.Unloaded classGeneratorSingletonContainer;

    public WrapperPlugin(
            Class originInterface,
            String getWrapperMethodName,
            Class<W> wrapperClass,
            Predicate<TypeDescription> selectTypeCriteria,

            String wrapperFieldName,
            String classFactoryGeneratorFieldName,
            Class<? extends MetaClassFactory<W>> classFactoryGenerator) {

        this.originInterface = originInterface;
        this.getWrapperMethodName = getWrapperMethodName;
        this.wrapperClass = wrapperClass;
        this.selectTypeCriteria = selectTypeCriteria;
        this.wrapperFieldName = checkFieldName(wrapperFieldName);
        this.classFactoryGeneratorFieldName = checkFieldName(classFactoryGeneratorFieldName);
        this.classFactoryGenerator = classFactoryGenerator;
        this.classGeneratorSingletonContainer = createSingletonHolder(classFactoryGenerator);
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
     */
    public WrapperPlugin(
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
                typeDefinitions -> typeDefinitions
                        .getDeclaredAnnotations()
                        .stream()
                        .map(AnnotationDescription::getAnnotationType)
                        .anyMatch(new TypeDescription.ForLoadedType(typeMarker)::equals),
                wrapperFieldName,
                classFactoryGeneratorFieldName,
                classFactoryGenerator);
    }

    public WrapperPlugin(
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

    public WrapperPlugin(
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

    public WrapperPlugin(
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

    public WrapperPlugin(
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

    public WrapperPlugin(Builder<W> builder) {
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

    @Override
    public DynamicType.Builder<?> apply(
            DynamicType.Builder<?> builder,
            TypeDescription typeDescription,
            ClassFileLocator classFileLocator) {

        DynamicType.Builder<?> b = builder;

        if(!typeDescription.getInterfaces().contains(new TypeDescription.ForLoadedType(originInterface))) {
            b = b.implement(originInterface);
        }

        return b
                .defineField(wrapperFieldName, wrapperClass, Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC)
                .defineField(classFactoryGeneratorFieldName, Function.class, Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC)
                .invokable(ElementMatchers.isTypeInitializer())
                .intercept(new InitFieldUsingClassInstanceMethodImplementation(
                        classGeneratorSingletonContainer.getTypeDescription(),
                        classFactoryGeneratorFieldName,
                        classFactoryGenerator
                ))
                .invokable(ElementMatchers.isConstructor())
                .intercept(SuperMethodCall.INSTANCE.andThen(
                        new InitFieldWithConstructorFieldUsingThisImplementation(
                                classFactoryGeneratorFieldName, wrapperFieldName)))
                .define(new TypeDescription.ForLoadedType(originInterface)
                        .getDeclaredMethods()
                        .filter(ElementMatchers.named(getWrapperMethodName))
                        .getOnly())

                .intercept(FieldAccessor.ofField(wrapperFieldName))

                .require(classGeneratorSingletonContainer);
    }

    private DynamicType.Unloaded createSingletonHolder(Class<? extends MetaClassFactory<W>> classFactoryGenerator) {
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

        public WrapperPlugin<W> build() {

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

            return new WrapperPlugin<>(
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
