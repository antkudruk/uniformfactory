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

package com.github.antkudruk.uniformfactory.methodlist.descriptors;

import com.github.antkudruk.uniformfactory.base.AbstractMethodDescriptorImpl;
import com.github.antkudruk.uniformfactory.base.Enhancer;
import com.github.antkudruk.uniformfactory.base.exception.WrongTypeException;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.methodcollection.ElementFactory;
import com.github.antkudruk.uniformfactory.methodcollection.GetterElementFactory;
import com.github.antkudruk.uniformfactory.methodlist.enhancers.MethodListEnhancer;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MethodListDescriptor<F, R> extends AbstractMethodDescriptorImpl {

    private final Class<F> functionalInterface;
    private final Method wrapperMethod;
    private final Method functionalClassMethod;
    private final ElementFactory<F> elementFactory;

    private MethodListDescriptor(BuilderInterface<F, R> builder) {
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
        this.elementFactory = builder.getElementFactory();
        validate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enhancer getEnhancer(TypeDescription originType) throws ClassGeneratorException {
        List<DynamicType.Unloaded> functionalMapperClasses = new ArrayList<>();
        for (MethodDescription originMethod : memberSelector.getMethods(originType)) {
            functionalMapperClasses.add(
                    elementFactory.getMethodElement(originType, originMethod).build(originType)
            );
        }

        for (FieldDescription field : memberSelector.getFields(originType)) {


            functionalMapperClasses.add(
                    elementFactory.getFieldElement(originType, field).build(originType)
            );
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

    public interface BuilderInterface<F, R>
            extends AbstractMethodDescriptorImpl.BuilderInterface<R> {

        Class<F> getFunctionalInterface();

        /**
         * @return Factory that returns class factory for each method or field.
         */
        ElementFactory<F> getElementFactory();
    }

    public static class Builder<F, R>
            extends AbstractMethodDescriptorImpl.Builder<R, Builder<F, R>>
            implements BuilderInterface<F, R> {

        private final Class<F> functionalInterface;
        private ElementFactory<F> elementFactory;

        public Builder(Class<F> functionalInterface, Method wrapperMethod, Class<R> methodResultType) {
            super(wrapperMethod, methodResultType);
            this.functionalInterface = functionalInterface;
        }

        @Override
        public Class<F> getFunctionalInterface() {
            return functionalInterface;
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
        public MethodListDescriptor<F, R> build() {
            return new MethodListDescriptor<>(this);
        }
    }

    public static abstract class IntermediateShortcutBuilder<F, R, T extends IntermediateShortcutBuilder<F, R, T>>
            extends AbstractMethodDescriptorImpl.ShortcutBuilder<R, T>
            implements BuilderInterface<F, R> {

        private Class<F> functionalInterface;
        private ElementFactory<F> elementFactory;

        public IntermediateShortcutBuilder(Class<F> functionalInterface, Method wrapperMethod, Class<R> methodResultType) {
            super(wrapperMethod, methodResultType);
            this.functionalInterface = functionalInterface;
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
        public MethodListDescriptor<F, R> build() {
            return new MethodListDescriptor<>(this);
        }
    }

    public static final class ShortcutBuilder<F, R>
            extends IntermediateShortcutBuilder<F, R, ShortcutBuilder<F, R>> {

        public ShortcutBuilder(Class<F> functionalInterface, Method wrapperMethod, Class<R> methodResultType) {
            super(functionalInterface, wrapperMethod, methodResultType);
        }

        /*
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
        */
    }
}
