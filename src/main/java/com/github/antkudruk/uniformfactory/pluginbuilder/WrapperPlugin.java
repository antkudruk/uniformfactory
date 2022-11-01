/*
    Copyright 2020 - Present Anton Kudruk

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

import com.github.antkudruk.uniformfactory.base.Enhancer;
import com.github.antkudruk.uniformfactory.base.bytecode.EmptyImplementation;
import com.github.antkudruk.uniformfactory.base.bytecode.InitFieldWithDefaultConstructorImplementation;
import com.github.antkudruk.uniformfactory.classfactory.EnhancerBasedEnhancer;
import com.github.antkudruk.uniformfactory.classfactory.WrapperEnhancer;
import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.AmbiguousGetWrapperMethodException;
import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.OriginInterfaceNotDefinedException;
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
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.util.*;
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

    private final Class<?> originInterface;
    private final Predicate<TypeDescription> selectTypeCriteria;
    private final List<WrapperDescriptor<?>> wrappers;
    private final Map<String, DynamicType.Unloaded<? extends MetaClassFactory<?>>> classGeneratorSingletonContainer;

    @SuppressWarnings("WeakerAccess")
    public WrapperPlugin(
            Class<?> originInterface,
            Predicate<TypeDescription> selectTypeCriteria,
            List<WrapperDescriptor<?>> wrappers
    ) {

        this.originInterface = originInterface;
        this.selectTypeCriteria = selectTypeCriteria;
        this.wrappers = wrappers;
        validate();
        this.classGeneratorSingletonContainer = wrappers
                .stream()
                .collect(Collectors.toMap(
                        WrapperDescriptor::getWrapperField,
                        e -> createSingletonHolder(e.getWrapperClassFactory())
                ));
    }

    private void validate() {
        if (originInterface == null) {
            throw new OriginInterfaceNotDefinedException();
        }

        if (selectTypeCriteria == null) {
            throw new SelectClassCriteriaNotDefinedException();
        }

        validateMethodNames();

        wrappers.forEach(e -> e.validateForOrigin(originInterface));
    }

    private void validateMethodNames() {
        Set<String> methodNames = new HashSet<>();
        wrappers
                .stream()
                .map(WrapperDescriptor::getMethodName)
                .peek(n -> {
                    if (methodNames.contains(n)) throw new AmbiguousGetWrapperMethodException(n);
                })
                .forEach(methodNames::add);
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
    @SuppressWarnings("WeakerAccess")
    public <W> WrapperPlugin(
            Class<?> originInterface,
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
     * @param <W> Adapter class
     */
    @SuppressWarnings("WeakerAccess")
    public <W> WrapperPlugin(
            Class<?> originInterface,
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
     * @param <W> Adapter class
     */
    @SuppressWarnings("unused")
    public <W> WrapperPlugin(
            Class<?> originInterface,
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
     * @param <W> Adapter class
     */
    @SuppressWarnings("unused")
    public <W> WrapperPlugin(
            Class<?> originInterface,
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
     * @param <W> Adapter class
     */
    @SuppressWarnings("unused")
    public <W> WrapperPlugin(
            Class<?> originInterface,
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
     * @param <W> Adapter class
     */
    @SuppressWarnings("unused")
    public <W> WrapperPlugin(
            Class<?> originInterface,
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

    @SuppressWarnings("unused")
    public <W> WrapperPlugin(Builder builder) {
        this(
                builder.originInterface,
                builder.selectClassCriteria,
                builder.wrappers
        );
    }

    @SuppressWarnings("unused")
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
     * @return Decision if the Gradle plugin has to apply this transformation
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
        if (!typeDescription.getInterfaces().contains(new TypeDescription.ForLoadedType(originInterface).asGenericType())) {
            builder = builder.implement(originInterface);
        }

        List<Enhancer> enhancer = wrappers
                .stream()
                .filter(e -> shouldImplement(typeDescription, e.getMethodName()))
                .map(this::getWrapperEnhancer)
                .collect(Collectors.toList());

        EnhancerBasedEnhancer enhancerBasedEnhancer = new EnhancerBasedEnhancer(enhancer);

        builder = builder
                .invokable(ElementMatchers.isTypeInitializer())
                .intercept(enhancerBasedEnhancer.addStaticInitiation(new EmptyImplementation()))
                .invokable(ElementMatchers.isConstructor())
                .intercept(enhancerBasedEnhancer.addInitiation(SuperMethodCall.INSTANCE))
                .declaredTypes(classGeneratorSingletonContainer
                        .values()
                        .stream()
                        .map(DynamicType::getTypeDescription)
                        .collect(Collectors.toList()));

        builder = enhancerBasedEnhancer.addMethod(builder);

        return builder.require(classGeneratorSingletonContainer.values().toArray(new DynamicType.Unloaded[0]));
    }

    private boolean shouldImplement(TypeDescription typeDescription, String getWrapperMethodName) {
        MethodDescription originMethodReturningWrapper = new TypeDescription.ForLoadedType(originInterface)
                .getDeclaredMethods()
                .filter(ElementMatchers.named(getWrapperMethodName))
                .getOnly();

        return implementsMethod(typeDescription, originMethodReturningWrapper);
    }

    private Enhancer getWrapperEnhancer(WrapperDescriptor<?> wrapperDescriptor) {
        String getWrapperMethodName = wrapperDescriptor.getMethodName();
        String wrapperFieldName = wrapperDescriptor.getWrapperField();
        Class<?> wrapperClass = wrapperDescriptor.getWrapperClass();
        String classFactoryGeneratorFieldName = wrapperDescriptor.getWrapperFactoryField();
        Class<? extends MetaClassFactory<?>> classFactoryGenerator = wrapperDescriptor.getWrapperClassFactory();

        return new WrapperEnhancer(
                originInterface,
                classFactoryGenerator,
                classGeneratorSingletonContainer
                        .get(wrapperFieldName)
                        .getTypeDescription(),
                classFactoryGeneratorFieldName,
                wrapperClass,
                wrapperFieldName,
                getWrapperMethodName
        );
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

    private DynamicType.Unloaded<? extends MetaClassFactory<?>> createSingletonHolder(
            Class<? extends MetaClassFactory<?>> classFactoryGenerator) {
        ByteBuddy byteBuddy = new ByteBuddy();
        //noinspection unchecked,rawtypes
        return (DynamicType.Unloaded)byteBuddy
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

    public static class Builder {
        private Class<?> originInterface;
        private Predicate<TypeDescription> selectClassCriteria;

        private final List<WrapperDescriptor<?>> wrappers = new ArrayList<>();

        public Builder() {
        }

        public Builder setOriginInterface(Class<?> originInterface) {
            this.originInterface = originInterface;
            return this;
        }

        public Builder setTypeMarker(Class<? extends Annotation> typeMarker) {
            this.selectClassCriteria = (td) -> td.getDeclaredAnnotations().isAnnotationPresent(typeMarker);
            return this;
        }

        public Builder setSelectClassCriteria(Predicate<TypeDescription> selectClassCriteria) {
            this.selectClassCriteria = selectClassCriteria;
            return this;
        }

        public Builder clearWrappers() {
            wrappers.clear();
            return this;
        }

        public <W> WrapperDescriptor.ShortcutBuilder<Builder, W> addWrapper(Class<W> type) {
            return new WrapperDescriptor.ShortcutBuilder<>(this, type);
        }

        public <W> Builder addWrapperDescriptor(WrapperDescriptor<W> wrapperDescriptor) {
            wrappers.add(wrapperDescriptor);
            return this;
        }
    }
}
