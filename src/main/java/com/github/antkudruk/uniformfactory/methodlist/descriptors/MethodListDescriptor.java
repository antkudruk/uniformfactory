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
import com.github.antkudruk.uniformfactory.base.exception.WrongTypeException;
import com.github.antkudruk.uniformfactory.classfactory.ChildMethodDescriptionBuilderWrapper;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.methodlist.enhancers.MethodListEnhancer;
import lombok.Getter;
import lombok.experimental.Delegate;
import net.bytebuddy.description.type.TypeDescription;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Describes implementation to work with a few class member satisfying
 * particular criteria (for instance, marked with an annotation)
 *
 * @param <F> Type of the list element
 */
public class MethodListDescriptor<F> extends AbstractMethodCollectionDescriptor<F> {

    private final ListElementSource<F> elementSource;

    private MethodListDescriptor(Method wrapperMethod,
                                 Class<F> functionalInterface,
                                 ListElementSource<F> elementSource) {
        super(wrapperMethod, functionalInterface);
        this.elementSource = elementSource;
        validate();
    }

    private void validate () {
        if(getWrapperMethod().getReturnType() != List.class) {
            throw new WrongTypeException(List.class, getWrapperMethod().getReturnType());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enhancer getEnhancer(TypeDescription originType) throws ClassGeneratorException {
        return new MethodListEnhancer<>(
                "values",
                originType,
                wrapperMethod,
                elementSource.elements(originType));
    }

    @SuppressWarnings("unchecked")
    @Getter
    public static abstract class AbstractBuilder<F, T extends AbstractBuilder<F, T>>
            extends AbstractMethodCollectionDescriptor.AbstractBuilder<F, T>
            implements DefaultListElementSource.ShortcutBuilder.ParentBuilder<F> {

        private final Class<F> functionalInterface;
        private ListElementSource<F> elementSource;

        public AbstractBuilder(Class<F> functionalInterface, Method wrapperMethod) {
            super(wrapperMethod, functionalInterface);
            this.functionalInterface = functionalInterface;
        }

        public T setElementSource(ListElementSource<F> elementSource) {
            this.elementSource = elementSource;
            return (T) this;
        }

        public <R> DefaultListElementSource.ShortcutBuilder<T, F, R> defaultElementSource() {
            return new DefaultListElementSource.ShortcutBuilder<>(
                    (T) this,
                    functionalInterface);
        }

        @Override
        public MethodListDescriptor<F> build() {
            return new MethodListDescriptor<>(
                    wrapperMethod,
                    functionalInterface,
                    elementSource);
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
     * @param <F> Type of list element
     */
    public static class ShortcutBuilder<W, F>
            extends AbstractBuilder<F, ShortcutBuilder<W, F>> {

        @Delegate
        private final ChildMethodDescriptionBuilderWrapper<W> classFactoryReference;

        public ShortcutBuilder(
                ClassFactory.Builder<W> wrapperClass,
                Class<F> functionalInterface,
                Method wrapperMethod) {
            super(functionalInterface, wrapperMethod);
            classFactoryReference = new ChildMethodDescriptionBuilderWrapper<>(wrapperClass, this);
        }
    }
}
