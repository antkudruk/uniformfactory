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

import com.github.antkudruk.uniformfactory.base.AbstractMethodCollectionDescriptor;
import com.github.antkudruk.uniformfactory.base.Enhancer;
import com.github.antkudruk.uniformfactory.classfactory.ChildMethodDescriptionBuilderWrapper;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.methodlist.enhancers.MethodListEnhancer;
import lombok.Getter;
import lombok.experimental.Delegate;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes implementation to work with a few class member satisfying
 * particular criteria (for instance, marked with an annotation)
 *
 * @param <F> Type of the list element
 */
public class MethodListDescriptor<F> extends AbstractMethodCollectionDescriptor<F> {

    private MethodListDescriptor(BuilderInterface<F> builder) {
        super(builder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enhancer getEnhancer(TypeDescription originType) throws ClassGeneratorException {
        List<DynamicType.Unloaded<?>> functionalMapperClasses = new ArrayList<>();
        for (MethodDescription originMethod : memberSelector.getMethods(originType)) {
            functionalMapperClasses.add(
                    getElementFactory().getMethodElement(originType, originMethod).build(originType)
            );
        }

        for (FieldDescription field : memberSelector.getFields(originType)) {
            functionalMapperClasses.add(
                    getElementFactory().getFieldElement(originType, field).build(originType)
            );
        }

        return new MethodListEnhancer(
                "values",
                originType,
                wrapperMethod,
                functionalMapperClasses);
    }

    public interface BuilderInterface<F>
            extends AbstractMethodCollectionDescriptor.BuilderInterface<F> {
    }

    @Getter
    public static abstract class AbstractBuilder<F, T extends AbstractBuilder<F, T>>
            extends AbstractMethodCollectionDescriptor.AbstractBuilder<F, T>
            implements BuilderInterface<F> {

        private final Class<F> functionalInterface;

        public AbstractBuilder(Class<F> functionalInterface, Method wrapperMethod) {
            super(wrapperMethod);
            this.functionalInterface = functionalInterface;
        }

        @Override
        public MethodListDescriptor<F> build() {
            return new MethodListDescriptor<>(this);
        }
    }

    public static final class Builder<F> extends AbstractBuilder<F, Builder<F>> {
        public Builder(Class<F> functionalInterface, Method wrapperMethod) {
            super(functionalInterface, wrapperMethod);
        }
    }

    /**
     * List of methods
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
