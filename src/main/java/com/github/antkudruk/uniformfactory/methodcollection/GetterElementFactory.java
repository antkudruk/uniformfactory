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

import com.github.antkudruk.uniformfactory.base.AbstractMethodCollectionDescriptor;
import com.github.antkudruk.uniformfactory.base.ParameterMapperBuilder;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.SpecifiedFieldSelector;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.SpecifiedMethodSelector;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.ParameterBindersSource;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.HasParameterTranslator;
import com.github.antkudruk.uniformfactory.singleton.descriptors.ResultMapperCollection;
import lombok.experimental.Delegate;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;

/**
 *
 * Returns class factories to getter wrappers for method/field, no matter how
 * the field or method was chosen
 *
 * @param <F> Functional interface
 * @param <R> Method result type
 */
public class GetterElementFactory<F, R> implements ElementFactory<F> {

    private final Class<F> elementType;
    private final ResultMapperCollection<R> resultMapper;
    private final ParameterBindersSource parameterMapper;

    public GetterElementFactory(
            Class<F> elementType,
            ResultMapperCollection<R> resultMapper,
            ParameterBindersSource parameterMapper) {

        this.elementType = elementType;
        this.resultMapper = resultMapper;
        this.parameterMapper = parameterMapper;
    }

    @Override
    public ClassFactory<F> getFieldElement(
            TypeDescription origin, FieldDescription fieldDescription) {
        return new ClassFactory.Builder<>(elementType)
                .addMethodSingleton(
                        elementType.getDeclaredMethods()[0],
                        resultMapper.getWrapperReturnType()
                )
                .setMemberSelector(new SpecifiedFieldSelector(fieldDescription))
                .setParameterMapper(parameterMapper)
                .setResultMapper(resultMapper)
                .endMethodDescription()
                .build();
    }

    @Override
    public ClassFactory<F> getMethodElement(
            TypeDescription origin, MethodDescription originMethod) {
        return new ClassFactory.Builder<>(elementType)
                .addMethodSingleton(
                        elementType.getDeclaredMethods()[0],
                        resultMapper.getWrapperReturnType()
                )
                .setMemberSelector(new SpecifiedMethodSelector(originMethod))
                .setResultMapper(resultMapper)
                .setParameterMapper(parameterMapper)
                .endMethodDescription()
                .build();
    }

    /**
     *
     * @param <F> Functional interface
     * @param <R> Method return type
     * @param <T> This builder class
     */
    public static class AbstractBuilder<F, R, T extends AbstractBuilder<F, R, T>>
            implements ElementFactory.BuilderInterface<F>, HasParameterTranslator {

        private final Class<F> elementType;
        private ResultMapperCollection<R> resultMapper;
        @SuppressWarnings("unchecked")
        @Delegate
        private final ParameterMapperBuilder<T> parameterMapperBuilder = new ParameterMapperBuilder<>((T) this);

        public AbstractBuilder(Class<F> elementType, Class<R> resultType) {
            this.elementType = elementType;
            this.resultMapper = new ResultMapperCollection<>(resultType);
        }

        @SuppressWarnings("unchecked")
        public T setResultMapper(ResultMapperCollection<R> resultMapper) {
            this.resultMapper = resultMapper;
            return (T) this;
        }

        @Override
        public ElementFactory<F> build() {
            return new GetterElementFactory<>(
                    elementType,
                    resultMapper,
                    parameterMapperBuilder.getParameterMapper()
            );
        }
    }

    public static final class Builder<F, R> extends AbstractBuilder<F, R, Builder<F, R>> {
        public Builder(Class<F> elementType, Class<R> resultType) {
            super(elementType, resultType);
        }
    }

    public static final class ShortcutBuilder<M extends AbstractMethodCollectionDescriptor.BuilderInterface<F>, F, R>
            extends AbstractBuilder<F, R, ShortcutBuilder<M, F, R>> {
        @Delegate
        private final ElementFactoryBuilderParentReference<F, M> parentReference;

        public ShortcutBuilder(
                M builder,
                Class<F> elementType,
                Class<R> resultType) {
            super(elementType, resultType);
            parentReference = new ElementFactoryBuilderParentReference<>(
                    builder,
                    this);
        }
    }
}
