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
import com.github.antkudruk.uniformfactory.methodcollection.ElementFactory;
import com.github.antkudruk.uniformfactory.methodcollection.GetterElementFactory;
import com.github.antkudruk.uniformfactory.methodmap.enhancers.MemberEntry;
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
import java.util.Optional;
import java.util.function.Function;

/**
 *
 * Supposing origin class has multiple methods marked with {@code markerAnnotation}.
 * Method defined with {@code MethodMapDescriptor} returns a map of objects that
 * have methods calling corresponding rigin method.
 *
 */
public class MethodMapDescriptor<F, R> extends AbstractMethodDescriptorImpl<R> {

    private final Function<MethodDescription, StackManipulation> methodKeyGetter;
    private final Function<FieldDescription, StackManipulation> fieldKeyGetter;
    private final Class<F> functionalInterface;
    private final Method wrapperMethod;
    private final Method functionalClassMethod;
    private final ElementFactory<F> elementFactory;

    public MethodMapDescriptor(BuilderInterface<F, R> builder) {
        super(builder);

        wrapperMethod = builder.getWrapperMethod();

        this.functionalInterface = builder.getFunctionalInterface();

        this.methodKeyGetter = builder.getMethodKeyGetter();
        this.fieldKeyGetter = builder.getFieldKeyGetter();

        if (functionalInterface.getDeclaredMethods().length != 1) {
            throw new RuntimeException("Functional interface must contain exactly one method.");
        }

        functionalClassMethod = functionalInterface.getDeclaredMethods()[0];
        elementFactory = builder.getElementFactory();
        validate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enhancer getEnhancer(TypeDescription originType) throws ClassGeneratorException {

        List<MemberEntry> functionalMapperClasses = new ArrayList<>();

        for (MethodDescription originMethod : memberSelector.getMethods(originType)) {
            functionalMapperClasses.add(new MemberEntry(
                    methodKeyGetter.apply(originMethod),
                    elementFactory.getMethodElement(originType, originMethod).build(originType)
            ));
        }

        for (FieldDescription field : memberSelector.getFields(originType)) {
            functionalMapperClasses.add(new MemberEntry(
                    fieldKeyGetter.apply(field),
                    elementFactory.getFieldElement(originType, field).build(originType)
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

    public interface BuilderInterface<F, R>
            extends AbstractMethodDescriptorImpl.BuilderInterface<R> {

        Class<F> getFunctionalInterface();
        Function<MethodDescription, StackManipulation>  getMethodKeyGetter();
        Function<FieldDescription, StackManipulation>  getFieldKeyGetter();

        /**
         * @return Factory that returns class factory for each method or field.
         */
        ElementFactory<F> getElementFactory();
    }

    public static class Builder<F, R>
            extends AbstractMethodDescriptorImpl.Builder<R, MethodMapDescriptor.Builder<F, R>>
            implements BuilderInterface<F, R> {

        private final Class<F> functionalInterface;
        private Function<MethodDescription, StackManipulation> methodKeyGetter;
        private Function<FieldDescription, StackManipulation> fieldKeyGetter;
        private ElementFactory<F> elementFactory;

        public Builder(Class<F> functionalInterface, Method wrapperMethod, Class<R> methodResultType) {
            super(wrapperMethod, methodResultType);
            this.functionalInterface = functionalInterface;
        }

        public Builder<F, R> setMethodKeyGetter(Function<MethodDescription, StackManipulation> methodKeyGetter) {
            this.methodKeyGetter = methodKeyGetter;
            return this;
        }

        public Builder<F, R> setFieldKeyGetter(Function<FieldDescription, StackManipulation> fieldKeyGetter) {
            this.fieldKeyGetter = fieldKeyGetter;
            return this;
        }

        public <A extends Annotation> Builder<F, R> setMarkerAnnotation(Class<A> marker, Function<A, String> keyGetter) {
            setMarkerAnnotation(marker);
            setMethodKeyGetter(md -> new TextConstant(keyGetter.apply(md.getDeclaredAnnotations().ofType(marker).load())));
            setFieldKeyGetter(fd -> new TextConstant(keyGetter.apply(fd.getDeclaredAnnotations().ofType(marker).load())));
            return this;
        }

        @Override
        public Class<F> getFunctionalInterface() {
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

        public Builder<F, R> setElementFactory(ElementFactory<F> elementFactory) {
            this.elementFactory = elementFactory;
            return this;
        }

        @Override
        public ElementFactory<F> getElementFactory() {
            return Optional.ofNullable(elementFactory).orElse(
                    new GetterElementFactory<>(
                            getFunctionalInterface(),
                            getResultMapper(),
                            getParameterMapper()
                    )
            );
        }

        @Override
        public MethodMapDescriptor<F, R> build() {
            return new MethodMapDescriptor<>(this);
        }
    }

    public static abstract class IntermediateShortcutBuilder<F, R, T extends IntermediateShortcutBuilder<F, R, T>>
            extends AbstractMethodDescriptorImpl.ShortcutBuilder<R, T>
            implements BuilderInterface<F, R> {

        private final Class<F> functionalInterface;
        private Function<MethodDescription, StackManipulation> methodKeyGetter;
        private Function<FieldDescription, StackManipulation> fieldKeyGetter;
        private ElementFactory<F> elementFactory;

        public IntermediateShortcutBuilder(Class<F> functionalInterface, Method wrapperMethod, Class<R> methodResultType) {
            super(wrapperMethod, methodResultType);
            this.functionalInterface = functionalInterface;
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
        public ElementFactory<F> getElementFactory() {
            return Optional.ofNullable(elementFactory).orElse(
                    new GetterElementFactory<>(
                            getFunctionalInterface(),
                            getResultMapper(),
                            getParameterMapper()
                    )
            );
        }

        @Override
        public MethodMapDescriptor<F, R> build() {
            return new MethodMapDescriptor<>(this);
        }
    }

    public static final class ShortcutBuilder<F, R>
            extends IntermediateShortcutBuilder<F, R, ShortcutBuilder<F, R>> {

        public ShortcutBuilder(Class<F> functionalInterface, Method wrapperMethod, Class<R> methodResultType) {
            super(functionalInterface, wrapperMethod, methodResultType);
        }
    }
}
