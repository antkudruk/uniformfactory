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

package com.github.antkudruk.uniformfactory.methodmap.descriptors;

import com.github.antkudruk.uniformfactory.base.AbstractMethodDescriptorImpl;
import com.github.antkudruk.uniformfactory.base.Enhancer;
import com.github.antkudruk.uniformfactory.methodcollection.ElementGenerator;
import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.field.AccessFieldValue;
import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.method.AccessMethodInvocation;
import com.github.antkudruk.uniformfactory.base.exception.WrongTypeException;
import com.github.antkudruk.uniformfactory.exception.AmbiguousValueSourceException;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.methodmap.enhancers.MethodMapEnhancer;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * Supposing origin class has multiple methods marked with {@code markerAnnotation}.
 * Method defined with {@code MethodMapDescriptor} returns a map of objects that
 * have methods calling corresponding rigin method.
 *
 * @param <A> Annotation the wrapper method marked with
 */
public class MethodMapDescriptor<A extends Annotation> extends AbstractMethodDescriptorImpl {

    public static final String INTERMEDIATE_WRAPPER_FIELD_NAME = "intermediateWrapper";

    private final Function<A, String> keyGetter;
    private final Class functionalInterface;
    private final Method wrapperMethod;
    private final Method functionalClassMethod;

    private MethodMapDescriptor(BuilderInterface<A, ?> builder) {
        super(builder);

        wrapperMethod = builder.getWrapperMethod();

        this.functionalInterface = builder.getFunctionalInterface();

        this.keyGetter = builder.getKeyGetter();

        if (functionalInterface.getDeclaredMethods().length != 1) {
            throw new RuntimeException("Functional interface must contain exactly one method.");
        }

        functionalClassMethod = functionalInterface.getDeclaredMethods()[0];
        validate();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enhancer getEnhancer(TypeDescription originType) throws ClassGeneratorException {

        Map<String, DynamicType.Unloaded> functionalMapperClasses = new HashMap<>();

        for (MethodDescription originMethod : originType.getDeclaredMethods()
                .filter(ElementMatchers.isAnnotatedWith(markerAnnotation))) {

            String k = keyGetter.apply((A) originMethod
                    .getDeclaredAnnotations()
                    .ofType(markerAnnotation).load());

            if (functionalMapperClasses.containsKey(k)) {
                throw new AmbiguousValueSourceException(null);
            }

            DynamicType.Unloaded functionalWrapperClass
                    = ElementGenerator.INSTANCE.generate(
                    originType,
                    new TypeDescription.ForLoadedType(functionalInterface),
                    AccessMethodInvocation.INSTANCE.generateClass(
                            originType,
                            resultMapper.getTranslatorOrThrow(originMethod.getReturnType().asErasure()),
                            originMethod,
                            functionalClassMethod,
                            parameterMapper.getParameterBinders(originMethod)
                    ),
                    MethodCall::withAllArguments,
                    INTERMEDIATE_WRAPPER_FIELD_NAME
            );

            functionalMapperClasses.put(k, functionalWrapperClass);
        }

        for (FieldDescription field : originType.getDeclaredFields()
                .filter(ElementMatchers.isAnnotatedWith(markerAnnotation))) {
            String k = keyGetter.apply((A) field
                    .getDeclaredAnnotations()
                    .ofType(markerAnnotation).load());

            if (functionalMapperClasses.containsKey(k)) {
                throw new AmbiguousValueSourceException(null);
            }

            DynamicType.Unloaded functionalWrapperClass = ElementGenerator.INSTANCE.generate(
                    originType,
                    new TypeDescription.ForLoadedType(functionalInterface),
                    AccessFieldValue.INSTANCE.generateClass(
                            originType,
                            resultMapper.getTranslatorOrThrow(field.getType().asErasure()),
                            field),
                    m -> m,
                    INTERMEDIATE_WRAPPER_FIELD_NAME
            );

            functionalMapperClasses.put(k, functionalWrapperClass);
        }

        return new MethodMapEnhancer(
                "values",
                originType,
                wrapperMethod,
                functionalMapperClasses);
    }

    private void validate() {
        if (!Map.class.isAssignableFrom(wrapperMethod.getReturnType())) {
            throw new WrongTypeException(Map.class, wrapperMethod.getReturnType());
        }

        if (functionalClassMethod.getReturnType() != resultMapper.getWrapperReturnType()) {
            throw new WrongTypeException(functionalClassMethod.getReturnType(), resultMapper.getWrapperReturnType());
        }
    }

    public interface BuilderInterface<M extends Annotation, R>
            extends AbstractMethodDescriptorImpl.BuilderInterface<M, R> {

        Class getFunctionalInterface();

        Function<M, String> getKeyGetter();
    }

    public static class Builder<M extends Annotation, R>
            extends AbstractMethodDescriptorImpl.Builder<M, R, MethodMapDescriptor.Builder<M, R>>
            implements BuilderInterface<M, R> {

        private Class functionalInterface;
        private Function<M, String> keyGetter;

        public Builder(Class<M> markerAnnotation, Method wrapperMethod, Class<R> methodResultType) {
            super(markerAnnotation, wrapperMethod, methodResultType);
        }

        public Builder<M, R> setFunctionalInterface(Class functionalInterface) {
            this.functionalInterface = functionalInterface;
            return this;
        }

        public Builder<M, R> setKeyGetter(Function<M, String> keyGetter) {
            this.keyGetter = keyGetter;
            return this;
        }

        @Override
        public Class getFunctionalInterface() {
            return functionalInterface;
        }

        @Override
        public Function<M, String> getKeyGetter() {
            return keyGetter;
        }

        @Override
        public MethodMapDescriptor<M> build() {
            return new MethodMapDescriptor<>(this);
        }
    }

    public static abstract class IntermediateShortcutBuilder<M extends Annotation, R, T extends IntermediateShortcutBuilder<M, R, T>>
            extends AbstractMethodDescriptorImpl.ShortcutBuilder<M, R, T>
            implements BuilderInterface<M, R> {

        private Class functionalInterface;
        private Function<M, String> keyGetter;

        public IntermediateShortcutBuilder(Class<M> markerAnnotation, Method wrapperMethod, Class<R> methodResultType) {
            super(markerAnnotation, wrapperMethod, methodResultType);
        }

        @SuppressWarnings("unchecked")
        public T setFunctionalInterface(Class functionalInterface) {
            this.functionalInterface = functionalInterface;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T setKeyGetter(Function<M, String> keyGetter) {
            this.keyGetter = keyGetter;
            return (T) this;
        }

        @Override
        public Class getFunctionalInterface() {
            return functionalInterface;
        }

        @Override
        public Function<M, String> getKeyGetter() {
            return keyGetter;
        }

        @Override
        public MethodMapDescriptor<M> build() {
            return new MethodMapDescriptor<>(this);
        }
    }

    public static final class ShortcutBuilder<M extends Annotation, R>
            extends IntermediateShortcutBuilder<M, R, ShortcutBuilder<M, R>> {

        public ShortcutBuilder(Class<M> markerAnnotation, Method wrapperMethod, Class<R> methodResultType) {
            super(markerAnnotation, wrapperMethod, methodResultType);
        }
    }
}
