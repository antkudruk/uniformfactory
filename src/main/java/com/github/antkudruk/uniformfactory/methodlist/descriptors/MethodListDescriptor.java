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

package com.github.antkudruk.uniformfactory.methodlist.descriptors;

import com.github.antkudruk.uniformfactory.base.AbstractMethodDescriptorImpl;
import com.github.antkudruk.uniformfactory.base.Enhancer;
import com.github.antkudruk.uniformfactory.base.exception.WrongTypeException;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.methodcollection.ElementGenerator;
import com.github.antkudruk.uniformfactory.methodlist.enhancers.MethodListEnhancer;
import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.field.AccessFieldValue;
import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.method.AccessMethodInvocation;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MethodListDescriptor<A extends Annotation> extends AbstractMethodDescriptorImpl {

    private static final String INTERMEDIATE_WRAPPER_FIELD_NAME = "intermediateWrapper";

    private final Class functionalInterface;
    private final Method wrapperMethod;
    private final Method functionalClassMethod;

    private MethodListDescriptor(BuilderInterface<A, ?> builder) {
        super(builder);

        wrapperMethod = builder.getWrapperMethod();

        this.functionalInterface = builder.getFunctionalInterface();

        if (functionalInterface == null) {
            throw new RuntimeException("You haven't defined a functional interface");
        }

        if (functionalInterface.getDeclaredMethods().length != 1) {
            throw new RuntimeException("Functional interface must contain exactly one method.");
        }

        functionalClassMethod = functionalInterface.getDeclaredMethods()[0];
        validate();
    }

    @Override
    public Enhancer getEnhancer(TypeDescription originType) throws ClassGeneratorException {
        List<DynamicType.Unloaded> functionalMapperClasses = new ArrayList<>();

        for (MethodDescription originMethod : originType.getDeclaredMethods()
                .filter(ElementMatchers.isAnnotatedWith(markerAnnotation))) {

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

            functionalMapperClasses.add(functionalWrapperClass);
        }

        for (FieldDescription field : originType.getDeclaredFields()
                .filter(ElementMatchers.isAnnotatedWith(markerAnnotation))) {

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

            functionalMapperClasses.add(functionalWrapperClass);
        }

        return new MethodListEnhancer(
                "values",
                originType,
                wrapperMethod,
                functionalMapperClasses);
    }


    private void validate() {
        if (!List.class.isAssignableFrom(wrapperMethod.getReturnType())) {
            throw new WrongTypeException(List.class, wrapperMethod.getReturnType());
        }

        if (functionalClassMethod.getReturnType() != resultMapper.getWrapperReturnType()) {
            throw new WrongTypeException(functionalClassMethod.getReturnType(), resultMapper.getWrapperReturnType());
        }
    }

    public interface BuilderInterface<M extends Annotation, R>
            extends AbstractMethodDescriptorImpl.BuilderInterface<M, R> {

        Class getFunctionalInterface();
    }

    public static class Builder<M extends Annotation, R>
            extends AbstractMethodDescriptorImpl.Builder<M, R, Builder<M, R>>
            implements BuilderInterface<M, R> {

        private Class functionalInterface;

        public Builder(Class<M> markerAnnotation, Method wrapperMethod, Class<R> methodResultType) {
            super(markerAnnotation, wrapperMethod, methodResultType);
        }

        public Builder<M, R> setFunctionalInterface(Class functionalInterface) {
            this.functionalInterface = functionalInterface;
            return this;
        }

        @Override
        public Class getFunctionalInterface() {
            return functionalInterface;
        }

        @Override
        public MethodListDescriptor<M> build() {
            return new MethodListDescriptor<>(this);
        }
    }

    public static abstract class IntermediateShortcutBuilder<M extends Annotation, R, T extends IntermediateShortcutBuilder<M, R, T>>
            extends AbstractMethodDescriptorImpl.ShortcutBuilder<M, R, T>
            implements BuilderInterface<M, R> {

        private Class functionalInterface;

        public IntermediateShortcutBuilder(Class<M> markerAnnotation, Method wrapperMethod, Class<R> methodResultType) {
            super(markerAnnotation, wrapperMethod, methodResultType);
        }

        @SuppressWarnings("unchecked")
        public T setFunctionalInterface(Class functionalInterface) {
            this.functionalInterface = functionalInterface;
            return (T) this;
        }

        @Override
        public Class getFunctionalInterface() {
            return functionalInterface;
        }

        @Override
        public MethodListDescriptor<M> build() {
            return new MethodListDescriptor<>(this);
        }
    }

    public static final class ShortcutBuilder<M extends Annotation, R>
            extends IntermediateShortcutBuilder<M, R, ShortcutBuilder<M, R>> {

        public ShortcutBuilder(Class<M> markerAnnotation, Method wrapperMethod, Class<R> methodResultType) {
            super(markerAnnotation, wrapperMethod, methodResultType);
        }
    }
}
