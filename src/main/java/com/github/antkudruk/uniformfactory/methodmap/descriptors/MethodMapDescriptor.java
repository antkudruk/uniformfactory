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
import com.github.antkudruk.uniformfactory.classfactory.ChildMethodDescriptionBuilderWrapper;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.methodcollection.ElementFactory;
import com.github.antkudruk.uniformfactory.methodmap.enhancers.MemberEntry;
import com.github.antkudruk.uniformfactory.base.exception.WrongTypeException;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.methodmap.enhancers.MethodMapEnhancer;
import lombok.experimental.Delegate;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
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
 * @param <F> Type of the map values
 *
 */
public class MethodMapDescriptor<F> extends AbstractMethodDescriptorImpl {

    private final Function<MethodDescription, StackManipulation> methodKeyGetter;
    private final Function<FieldDescription, StackManipulation> fieldKeyGetter;
    private final Class<F> functionalInterface;
    private final Method wrapperMethod;
    private final ElementFactory<F> elementFactory;

    public MethodMapDescriptor(BuilderInterface<F> builder) {
        super(builder);

        wrapperMethod = builder.getWrapperMethod();

        this.functionalInterface = builder.getFunctionalInterface();

        this.methodKeyGetter = builder.getMethodKeyGetter();
        this.fieldKeyGetter = builder.getFieldKeyGetter();

        if (functionalInterface.getDeclaredMethods().length != 1) {
            throw new RuntimeException("Functional interface must contain exactly one method.");
        }

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
    }

    public interface BuilderInterface<F>
            extends AbstractMethodDescriptorImpl.BuilderInterface {

        /**
         *
         * @return Type of the map values
         */
        Class<F> getFunctionalInterface();

        /**
         *
         * @return Operations to get key for a method
         */
        Function<MethodDescription, StackManipulation>  getMethodKeyGetter();

        /**
         *
         * @return Operations to get key for a field
         */
        Function<FieldDescription, StackManipulation>  getFieldKeyGetter();

        /**
         * @return Factory that returns class factory for each method or field.
         */
        ElementFactory<F> getElementFactory();
    }

    @SuppressWarnings("unchecked")
    public static abstract class AbstractBuilder<F, T extends AbstractBuilder<F, T>>
            extends AbstractMethodDescriptorImpl.AbstractBuilder<T>
            implements BuilderInterface<F> {

        private final Class<F> functionalInterface;
        private Function<MethodDescription, StackManipulation> methodKeyGetter;
        private Function<FieldDescription, StackManipulation> fieldKeyGetter;
        private ElementFactory<F> elementFactory;

        public AbstractBuilder(Class<F> functionalInterface, Method wrapperMethod) {
            super(wrapperMethod);
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

        public T setElementFactory(ElementFactory<F> elementFactory) {
            this.elementFactory = elementFactory;
            return (T) this;
        }

        @Override
        public ElementFactory<F> getElementFactory() {
            return elementFactory;
        }

        @Override
        public MethodMapDescriptor<F> build() {
            return new MethodMapDescriptor<>(this);
        }
    }

    public static final class Builder<F> extends AbstractBuilder<F, Builder<F>> {
        public Builder(Class<F> functionalInterface, Method wrapperMethod) {
            super(functionalInterface, wrapperMethod);
        }
    }

    /**
     * Map of methods
     *
     * @param <F> Type of a list element
     */
    public static class ShortcutBuilder<W, F>
            extends AbstractBuilder<F, ShortcutBuilder<W, F>> {

        @Delegate
        private final ChildMethodDescriptionBuilderWrapper<W> classFactoryReference;

        public ShortcutBuilder(
                ClassFactory.ShortcutBuilder<W> wrapperClass,
                Class<F> functionalInterface,
                Method wrapperMethod) {
            super(functionalInterface, wrapperMethod);
            classFactoryReference = new ChildMethodDescriptionBuilderWrapper<>(wrapperClass, this);
        }
    }
}
