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

package com.github.antkudruk.uniformfactory.classfactory;

import com.github.antkudruk.uniformfactory.base.Enhancer;
import com.github.antkudruk.uniformfactory.base.MethodDescriptor;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.methodlist.descriptors.MethodListDescriptor;
import com.github.antkudruk.uniformfactory.methodmap.descriptors.MethodMapDescriptor;
import com.github.antkudruk.uniformfactory.setter.descriptors.SetterDescriptor;
import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.Constants;
import com.github.antkudruk.uniformfactory.singleton.descriptors.MethodSingletonDescriptor;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Generates wrapper classes for a provided domain class.
 *
 * @param <W> Wrapper class.
 */
public class ClassFactory<W> {

    private final Class<W> wrapperInterface;
    private final Map<Method, MethodDescriptor> methodDescriptorBuilders;

    public ClassFactory(Builder<W> builder) throws ClassFactoryException {

        this.wrapperInterface = builder.wrapperInterface;
        this.methodDescriptorBuilders = builder.methodDescriptors;
        checkIfAllMethodsDescribed();
    }

    /**
     * For a class defined by {@code originClass} type description creates a wrapper class.
     *
     * @param originClass Class to generate the builder for.
     * @return Wrapper cclass for the origin argument
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
            throw new RuntimeException("The following methods are irrelevant\n"
                    + message);
        }
    }

    private void checkAndThrow(Set<Method> interfaceMethods,
                               Set<Method> describedMethods)
            throws WrapperMethodNotDescribed {

        List<String> missingMethodNames = describedMethods
                .stream()
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

        public ClassFactory<W> build() throws ClassFactoryException {
            return new ClassFactory<>(this);
        }
    }

    public static class ShortcutBuilder<W> extends Builder<W> {
        public ShortcutBuilder(Class<W> wrapperInterface) {
            super(wrapperInterface);
        }

        public <R> MethodSingletonBuilder<R> addMethodSingleton(
                Method wrapperMethod, Class<R> resultClass) {
            return new MethodSingletonBuilder<>(wrapperMethod, resultClass);
        }

        public <R> MethodMapBuilder<R> addMethodMap(
                Method wrapperMethod, Class<R> resultClass) {
            return new MethodMapBuilder<>(wrapperMethod, resultClass);
        }

        public <R> MethodListBuilder<R> addMethodList(
                Method wrapperMethod, Class<R> resultClass) {
            return new MethodListBuilder<>(wrapperMethod, resultClass);
        }

        public <R> SetterBuilder<R> addSetter(
                Method wrapperMethod, Class<R> resultClass) {
            return new SetterBuilder<>(wrapperMethod, resultClass);
        }

        public class MethodSingletonBuilder<R>
                extends MethodSingletonDescriptor.IntermediateShortcutBuilder<R, MethodSingletonBuilder<R>> {

            MethodSingletonBuilder(Method wrapperMethod, Class<R> methodResultType) {
                super(wrapperMethod, methodResultType);
            }

            public ShortcutBuilder<W> endMethodDescription() {
                ShortcutBuilder.this.addMethodDescriptor(this.build());
                return ShortcutBuilder.this;
            }
        }

        public class MethodMapBuilder<R>
                extends MethodMapDescriptor.IntermediateShortcutBuilder<R, MethodMapBuilder<R>> {

            MethodMapBuilder(Method wrapperMethod, Class<R> methodResultType) {
                super(wrapperMethod, methodResultType);
            }

            public ShortcutBuilder<W> endMethodDescription() {
                ShortcutBuilder.this.addMethodDescriptor(this.build());
                return ShortcutBuilder.this;
            }
        }

        public class MethodListBuilder<R>
                extends MethodListDescriptor.IntermediateShortcutBuilder<R, MethodListBuilder<R>> {

            MethodListBuilder(Method wrapperMethod, Class<R> methodResultType) {
                super(wrapperMethod, methodResultType);
            }

            public ShortcutBuilder<W> endMethodDescription() {
                ShortcutBuilder.this.addMethodDescriptor(this.build());
                return ShortcutBuilder.this;
            }
        }

        public class SetterBuilder<R> extends SetterDescriptor.IntermediateShortcutBuilder<R, SetterBuilder<R>> {

            SetterBuilder(Method wrapperMethod, Class<R> methodResultType) {
                super(wrapperMethod, methodResultType);
            }

            public ShortcutBuilder<W> endMethodDescription() {
                ShortcutBuilder.this.addMethodDescriptor(this.build());
                return ShortcutBuilder.this;
            }
        }
    }
}
