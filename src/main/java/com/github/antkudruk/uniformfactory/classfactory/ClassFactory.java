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

package com.github.antkudruk.uniformfactory.classfactory;

import com.github.antkudruk.uniformfactory.base.Enhancer;
import com.github.antkudruk.uniformfactory.base.MethodDescriptor;
import com.github.antkudruk.uniformfactory.base.bytecode.ReturnConstructedValueImplementation;
import com.github.antkudruk.uniformfactory.container.WrapperFactory;
import com.github.antkudruk.uniformfactory.container.WrapperMetaFactoryImpl;
import com.github.antkudruk.uniformfactory.exception.AlienMethodException;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.methodlist.descriptors.MethodListDescriptor;
import com.github.antkudruk.uniformfactory.methodmap.descriptors.MethodMapDescriptor;
import com.github.antkudruk.uniformfactory.setter.descriptors.SetterDescriptor;
import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.Constants;
import com.github.antkudruk.uniformfactory.singleton.descriptors.MethodSingletonDescriptor;
import com.github.antkudruk.uniformfactory.stackmanipulation.BbImplementationMethodDescriptor;
import lombok.Getter;
import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Generates wrapper classes for a provided domain class.
 *
 * @param <W> Wrapper class.
 */
public class ClassFactory<W> {

    @Getter
    private final Class<W> wrapperInterface;
    private final Map<Method, MethodDescriptor> methodDescriptorBuilders;

    public ClassFactory(Builder<W> builder) throws ClassFactoryException {

        this.wrapperInterface = builder.wrapperInterface;
        this.methodDescriptorBuilders = builder.methodDescriptors;
        checkIfAllMethodsDescribed();
        validate();
    }

    /**
     * For a class defined by {@code originClass} type description creates a wrapper class.
     *
     * @param originClass Class to generate the builder for.
     * @return Wrapper class for the origin argument
     * @throws ClassGeneratorException Thrown
     */
    public DynamicType.Unloaded<W> build(TypeDescription originClass)
            throws ClassGeneratorException {

        Map<Method, Enhancer> enhancers = new HashMap<>();

        for (Map.Entry<Method, MethodDescriptor> entry : methodDescriptorBuilders.entrySet()) {
            enhancers.put(
                    entry.getKey(),
                    entry.getValue().getEnhancer(originClass));
        }

        EnhancerBasedEnhancer enhancerBasedEnhancer = new EnhancerBasedEnhancer(enhancers.values());

        DynamicType.Builder<W> bbBuilder = new ByteBuddy()
                .subclass(wrapperInterface, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                .defineConstructor(Visibility.PUBLIC)
                .withParameters(originClass)
                .intercept(enhancerBasedEnhancer.addInitiation(
                        initialConstructorImplementation()
                ))
                .defineProperty(Constants.ORIGIN_FIELD_NAME, originClass, true);

        bbBuilder = enhancerBasedEnhancer.addMethod(bbBuilder);

        return bbBuilder.make();
    }

    /**
     *
     *
     * @param originClass
     * @return A function that creates an adapter for the consuming object
     * @param <S> Origin class
     * @throws ClassGeneratorException
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public <S> Function<S, W> buildWrapperFactory(Class<S> originClass) throws ClassGeneratorException {
        TypeDescription originTypeDescription = new TypeDescription.ForLoadedType(originClass);
        DynamicType.Unloaded<W> wrapperType = build(new TypeDescription.ForLoadedType(originClass));
        return new ByteBuddy()
                .subclass(Function.class, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                .defineConstructor(Visibility.PUBLIC)
                .intercept(
                        MethodCall.invoke(
                                new TypeDescription.ForLoadedType(Object.class)
                                        .getDeclaredMethods()
                                        .filter(ElementMatchers.isConstructor())
                                        .filter(ElementMatchers.takesNoArguments())
                                        .getOnly()
                        )
                )
                .defineMethod("apply", Object.class, Visibility.PUBLIC)
                .withParameters(Object.class)
                .intercept(
                        new ReturnConstructedValueImplementation(
                                wrapperType.getTypeDescription(),
                                originTypeDescription
                        )
                )
                .require(wrapperType)
                .make()
                .load(getClass().getClassLoader())
                .getLoaded()
                .getConstructor()
                .newInstance();
    }

    /**
     * A smart version of the method {@code buildWrapperFactory}
     *
     * Returns a factory that caches wrapper classes for each origin type.
     *
     * Creates container to hold adapters for your objects.
     * @return New container instance
     */
    public WrapperFactory<W> buildWrapperFactory() {
        return WrapperMetaFactoryImpl.INSTANCE.get(this);
    }

    private void validate() throws ClassFactoryException {
        if(!Modifier.isPublic(wrapperInterface.getModifiers())) {
            throw new WrapperInterfaceIsNotPublic(wrapperInterface);
        }
    }

    private void checkIfAllMethodsDescribed() throws WrapperMethodNotDescribed {

        Set<Method> interfaceMethods = Stream.of(wrapperInterface
                .getDeclaredMethods())
                .collect(Collectors.toSet());

        Set<Method> descriptorMethods = methodDescriptorBuilders.values().stream()
                .map(MethodDescriptor::getWrapperMethod)
                .collect(Collectors.toSet());

        checkIfMethodsRelatedToInterfaceAndThrow(descriptorMethods, wrapperInterface);
        checkAndThrow(descriptorMethods, interfaceMethods);
    }

    private void checkIfMethodsRelatedToInterfaceAndThrow(
            Set<Method> set, Class<?> wrapperClass)
            throws WrapperMethodNotDescribed {
        String message = set.stream()
                .filter(t -> !t.getDeclaringClass().isAssignableFrom(wrapperClass))
                .map((Method t) -> "Declared method "
                        + t.getDeclaringClass().getSimpleName()
                        + "#"
                        + t.getName()
                        + " is not related to the wrapper interface "
                        + wrapperClass.getSimpleName()
                        + ".")
                .collect(Collectors.joining("\n"));

        if (message.length() != 0) {
            throw new AlienMethodException(message);
        }
    }

    private void checkAndThrow(Set<Method> interfaceMethods,
                               Set<Method> describedMethods)
            throws WrapperMethodNotDescribed {

        List<String> missingMethodNames = describedMethods
                .stream()
                .filter(d -> !"getOrigin".equals(d.getName()))
                .filter(d -> Modifier.isAbstract(d.getModifiers()))
                .filter(d -> !interfaceMethods.contains(d))
                .map(Method::getName)
                .collect(Collectors.toList());

        if (!missingMethodNames.isEmpty()) {
            throw new WrapperMethodNotDescribed(null,
                    missingMethodNames.toArray(new String[0]));
        }
    }

    private Implementation.Composable initialConstructorImplementation() {
        try {
            return MethodCall.invoke(wrapperInterface.isInterface()
                    ? Object.class.getConstructor()
                    : wrapperInterface.getConstructor())
                    .andThen(FieldAccessor.ofField(Constants.ORIGIN_FIELD_NAME).setsArgumentAt(0));
        } catch (NoSuchMethodException ex) {
            // Object class is guaranteed to have default constructor
            throw new RuntimeException(ex);
        }
    }

    /**
     * Describes Wrapper Class Factory.
     *
     * @param <W> Wrapper class
     */
    public static class Builder<W> {

        private final Class<W> wrapperInterface;
        private final Map<Method, MethodDescriptor> methodDescriptors = new HashMap<>();

        public Builder(Class<W> wrapperInterface) {
            this.wrapperInterface = wrapperInterface;
        }

        public Builder<W> addMethodDescriptor(MethodDescriptor methodDescriptor) {
            methodDescriptors.put(methodDescriptor.getWrapperMethod(), methodDescriptor);
            return this;
        }

        public <R> MethodSingletonDescriptor.ShortcutBuilder<W, R> addMethodSingleton(
                Method wrapperMethod, Class<R> resultClass) {
            return new MethodSingletonDescriptor.ShortcutBuilder<>(this, wrapperMethod, resultClass);
        }

        /**
         * Adds method descriptor to describe method map
         *
         * @param wrapperMethod Method to return the map
         * @param functionalInterface Type of the map element
         * @param <F> Type of the map element
         * @return Builder to describe method map
         */
        public <F> MethodMapDescriptor.ShortcutBuilder<W, F> addMethodMap(
                Method wrapperMethod,
                Class<F> functionalInterface) {
            return new MethodMapDescriptor.ShortcutBuilder<>(this, functionalInterface, wrapperMethod);
        }

        /**
         * Adds method descriptor to describe method list
         *
         * @param wrapperMethod Method to return the list
         * @param functionalInterface Type of the list element
         * @param <F> Type of the list element
         * @return Builder to describe method list
         */
        public <F> MethodListDescriptor.ShortcutBuilder<W, F> addMethodList(
                Method wrapperMethod, Class<F> functionalInterface) {
            return new MethodListDescriptor.ShortcutBuilder<>(
                    this,
                    functionalInterface,
                    wrapperMethod);
        }

        public <R> SetterDescriptor.ShortcutBuilder<W, R> addSetter(
                Method wrapperMethod) {
            return new SetterDescriptor.ShortcutBuilder<>(this, wrapperMethod);
        }

        public BbImplementationMethodDescriptor.ShortcutBuilder<W> addByteBuddyImplementation(Method wrapperMethod) {
            return new BbImplementationMethodDescriptor.ShortcutBuilder<>(this, wrapperMethod);
        }

        public ClassFactory<W> build() throws ClassFactoryException {
            return new ClassFactory<>(this);
        }
    }
}
