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

package com.github.antkudruk.uniformfactory.methodcollection;

import com.github.antkudruk.uniformfactory.base.Builds;
import com.github.antkudruk.uniformfactory.base.ParameterMapperBuilder;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.SpecifiedFieldSelector;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.ParameterBindersSource;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.HasParameterTranslator;
import lombok.experimental.Delegate;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;

/**
 * Element factory for setter field
 * @param <F> Interface that has the setter method
 */
public class SetterElementFactory<F> implements ElementFactory<F>, HasParameterTranslator {

    private final Class<F> elementType;

    @Delegate
    private final ParameterMapperBuilder<SetterElementFactory<F>> parameterMapperBuilder
            = new ParameterMapperBuilder<>(this);

    public SetterElementFactory(Class<F> elementType, ParameterBindersSource parameterBindersSource) {
        this.elementType = elementType;
        parameterMapperBuilder.setParameterMapper(parameterBindersSource);
    }

    @Override
    public ClassFactory<F> getFieldElement(
            TypeDescription origin,
            FieldDescription fieldDescription) {
        return new ClassFactory.Builder<>(elementType)
                .addSetter(elementType.getDeclaredMethods()[0])
                .setParameterMapper(parameterMapperBuilder.getParameterMapper())
                .setMemberSelector(new SpecifiedFieldSelector(fieldDescription))
                .endMethodDescription()
                .build();
    }

    @Override
    public ClassFactory<F> getMethodElement(
            TypeDescription origin,
            MethodDescription methodDescription) {
            throw new RuntimeException("Not Implemented");  // TODO: Replace with a useful implementation
    }

    @SuppressWarnings("unchecked")
    public static abstract class AbstractBuilder<F, T extends AbstractBuilder<F, T>>
            implements Builds<ElementFactory<F>>, HasParameterTranslator {

        private final Class<F> elementType;
        @SuppressWarnings("unchecked")
        @Delegate
        private final ParameterMapperBuilder<T> parameterMapperBuilder = new ParameterMapperBuilder<>((T) this);

        public AbstractBuilder(Class<F> elementType) {
            this.elementType = elementType;
        }

        @Override
        public ElementFactory<F> build() {
            return new SetterElementFactory<>(elementType, parameterMapperBuilder.getParameterMapper());
        }
    }

    public static class Builder<F> extends AbstractBuilder<F, Builder<F>> {
        public Builder(Class<F> elementType) {
            super(elementType);
        }
    }

    public static class ShortcutBuilder<M extends ElementFactoryBuilderParentReference.ParentBuilder<F>, F>
            extends AbstractBuilder<F, ShortcutBuilder<M, F>> {

        @Delegate
        private final ElementFactoryBuilderParentReference<F, M> parentReference;

        public ShortcutBuilder(
                M builder,
                Class<F> elementType) {
            super(elementType);
            parentReference = new ElementFactoryBuilderParentReference<>(
                    builder,
                    this);
        }
    }
}
