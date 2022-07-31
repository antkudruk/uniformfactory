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

package com.github.antkudruk.uniformfactory.base;

import com.github.antkudruk.uniformfactory.methodcollection.ElementFactory;
import com.github.antkudruk.uniformfactory.methodcollection.GetterElementFactory;
import com.github.antkudruk.uniformfactory.methodcollection.SetterElementFactory;
import lombok.Getter;

import java.lang.reflect.Method;

@Getter
public abstract class AbstractMethodCollectionDescriptor<F> extends AbstractMethodDescriptorImpl {

    private final ElementFactory<F> elementFactory;
    private final Class<F> functionalInterface;

    public AbstractMethodCollectionDescriptor(BuilderInterface<F> builder) {
        super(builder);
        this.elementFactory = builder.getElementFactory();
        this.functionalInterface = builder.getFunctionalInterface();
        valiate();
    }

    private void valiate() {
        if (functionalInterface == null) {
            throw new RuntimeException("You haven't defined a functional interface");
        }
    }

    /**
     *
     * @param <F> Type of the collection element
     */
    public interface BuilderInterface<F> extends AbstractMethodDescriptorImpl.BuilderInterface {
        /**
         * @return Interface implemented by the list element
         */
        Class<F> getFunctionalInterface();

        /**
         * @return Factory that returns class factory for each method or field.
         */
        ElementFactory<F> getElementFactory();

        Object setElementFactory(ElementFactory<F> elementFactory);

        AbstractMethodCollectionDescriptor<F> build();
    }

    @SuppressWarnings("unchecked")
    @Getter
    public static abstract class AbstractBuilder<F, T extends AbstractBuilder<F, T>>
            extends AbstractMethodDescriptorImpl.AbstractBuilder<T>
            implements BuilderInterface<F> {

        private ElementFactory<F> elementFactory;

        public AbstractBuilder(Method wrapperMethod) {
            super(wrapperMethod);
        }

        public T setElementFactory(ElementFactory<F> elementFactory) {
            this.elementFactory = elementFactory;
            return (T) this;
        }

        public <R> GetterElementFactory.ShortcutBuilder<T, F, R> getterElementFactory(Class<F> elementType, Class<R> resultType) {
            return new GetterElementFactory.ShortcutBuilder<>((T) this, elementType, resultType);
        }

        public SetterElementFactory.ShortcutBuilder<T, F> setterElementFactory(Class<F> elementType) {
            return new SetterElementFactory.ShortcutBuilder<>((T) this, elementType);
        }
    }
}
