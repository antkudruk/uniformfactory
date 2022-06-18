/*
    Copyright 2020 - 2022 Anton Kudruk

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
import com.github.antkudruk.uniformfactory.methodmap.enhancers.MemberEntry;
import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.field.AccessFieldValue;
import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.method.AccessMethodInvocation;
import com.github.antkudruk.uniformfactory.base.exception.WrongTypeException;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.methodmap.enhancers.MethodMapEnhancer;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * Supposing origin class has multiple methods marked with {@code markerAnnotation}.
 * Method defined with {@code MethodMapDescriptor} returns a map of objects that
 * have methods calling corresponding rigin method.
 *
 */
public class MethodMapDescriptor<R> extends AbstractMethodDescriptorImpl {

    public static final String INTERMEDIATE_WRAPPER_FIELD_NAME = "intermediateWrapper";

    private final Function<MethodDescription, StackManipulation> methodKeyGetter;
    private final Function<FieldDescription, StackManipulation> fieldKeyGetter;
    private final Class functionalInterface;
    private final Method wrapperMethod;
    private final Method functionalClassMethod;

    public MethodMapDescriptor(BuilderInterface<R> builder) {
        super(builder);

        wrapperMethod = builder.getWrapperMethod();

        this.functionalInterface = builder.getFunctionalInterface();

        this.methodKeyGetter = builder.getMethodKeyGetter();
        this.fieldKeyGetter = builder.getFieldKeyGetter();

        if (functionalInterface.getDeclaredMethods().length != 1) {
            throw new RuntimeException("Functional interface must contain exactly one method.");
        }

        functionalClassMethod = functionalInterface.getDeclaredMethods()[0];
        validate();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Enhancer getEnhancer(TypeDescription originType) throws ClassGeneratorException {

        List<MemberEntry> functionalMapperClasses = new ArrayList<>();

        for (MethodDescription originMethod : memberSelector.getMethods(originType)) {
            functionalMapperClasses.add(new MemberEntry(
                    methodKeyGetter.apply(originMethod),
                    ElementGenerator.INSTANCE.generate(
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
                    )
            ));
        }

        for (FieldDescription field : memberSelector.getFields(originType)) {
            functionalMapperClasses.add(new MemberEntry(
                    fieldKeyGetter.apply(field),
                    ElementGenerator.INSTANCE.generate(
                            originType,
                            new TypeDescription.ForLoadedType(functionalInterface),
                            AccessFieldValue.INSTANCE.generateClass(
                                    originType,
                                    resultMapper.getTranslatorOrThrow(field.getType().asErasure()),
                                    field,
                                    wrapperMethod),
                            m -> m,
                            INTERMEDIATE_WRAPPER_FIELD_NAME
                    )
            ));
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

    public interface BuilderInterface<R>
            extends AbstractMethodDescriptorImpl.BuilderInterface<R> {

        Class getFunctionalInterface();
        Function<MethodDescription, StackManipulation>  getMethodKeyGetter();
        Function<FieldDescription, StackManipulation>  getFieldKeyGetter();
    }

    public static class Builder<R>
            extends AbstractMethodDescriptorImpl.Builder<R, MethodMapDescriptor.Builder<R>>
            implements BuilderInterface<R> {

        private Class functionalInterface;
        private Function<MethodDescription, StackManipulation> methodKeyGetter;
        private Function<FieldDescription, StackManipulation> fieldKeyGetter;

        public Builder(Method wrapperMethod, Class<R> methodResultType) {
            super(wrapperMethod, methodResultType);
        }

        public Builder<R> setFunctionalInterface(Class functionalInterface) {
            this.functionalInterface = functionalInterface;
            return this;
        }

        public Builder<R> setMethodKeyGetter(Function<MethodDescription, StackManipulation> methodKeyGetter) {
            this.methodKeyGetter = methodKeyGetter;
            return this;
        }

        public Builder<R> setFieldKeyGetter(Function<FieldDescription, StackManipulation> fieldKeyGetter) {
            this.fieldKeyGetter = fieldKeyGetter;
            return this;
        }

        public <A extends Annotation> Builder<R> setMarkerAnnotation(Class<A> marker, Function<A, String> keyGetter) {
            setMarkerAnnotation(marker);
            setMethodKeyGetter(md -> new TextConstant(keyGetter.apply(md.getDeclaredAnnotations().ofType(marker).load())));
            setFieldKeyGetter(fd -> new TextConstant(keyGetter.apply(fd.getDeclaredAnnotations().ofType(marker).load())));
            return this;
        }

        @Override
        public Class getFunctionalInterface() {
            return functionalInterface;
        }

        @Override
        public Function<MethodDescription, StackManipulation> getMethodKeyGetter() {
            return methodKeyGetter;
        }

        @Override
        public Function<FieldDescription, StackManipulation> getFieldKeyGetter() {
            return fieldKeyGetter;
        }

        @Override
        public MethodMapDescriptor<R> build() {
            return new MethodMapDescriptor<>(this);
        }
    }

    public static abstract class IntermediateShortcutBuilder<R, T extends IntermediateShortcutBuilder<R, T>>
            extends AbstractMethodDescriptorImpl.ShortcutBuilder<R, T>
            implements BuilderInterface<R> {

        private Class functionalInterface;
        private Function<MethodDescription, StackManipulation> methodKeyGetter;
        private Function<FieldDescription, StackManipulation> fieldKeyGetter;

        public IntermediateShortcutBuilder(Method wrapperMethod, Class<R> methodResultType) {
            super(wrapperMethod, methodResultType);
        }

        @SuppressWarnings("unchecked")
        public T setFunctionalInterface(Class functionalInterface) {
            this.functionalInterface = functionalInterface;
            return (T) this;
        }

        public T setMethodKeyGetter(Function<MethodDescription, StackManipulation> methodKeyGetter) {
            this.methodKeyGetter = methodKeyGetter;
            return (T) this;
        }

        public T setFieldKeyGetter(Function<FieldDescription, StackManipulation> fieldKeyGetter) {
            this.fieldKeyGetter = fieldKeyGetter;
            return (T) this;
        }

        public <A extends Annotation> T setMarkerAnnotation(Class<A> marker, Function<A, String> keyGetter) {
            setMarkerAnnotation(marker);
            setMethodKeyGetter(md -> new TextConstant(keyGetter.apply(md.getDeclaredAnnotations().ofType(marker).load())));
            setFieldKeyGetter(fd -> new TextConstant(keyGetter.apply(fd.getDeclaredAnnotations().ofType(marker).load())));
            return (T) this;
        }

        @Override
        public Class getFunctionalInterface() {
            return functionalInterface;
        }

        @Override
        public Function<MethodDescription, StackManipulation> getMethodKeyGetter() {
            return methodKeyGetter;
        }

        @Override
        public Function<FieldDescription, StackManipulation> getFieldKeyGetter() {
            return fieldKeyGetter;
        }

        @Override
        public MethodMapDescriptor<R> build() {
            return new MethodMapDescriptor<>(this);
        }
    }

    public static final class ShortcutBuilder<R>
            extends IntermediateShortcutBuilder<R, ShortcutBuilder<R>> {

        public ShortcutBuilder(Method wrapperMethod, Class<R> methodResultType) {
            super(wrapperMethod, methodResultType);
        }
    }
}
