package com.github.antkudruk.uniformfactory.methodcollection;

import com.github.antkudruk.uniformfactory.base.AbstractMethodCollectionDescriptor;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.SpecifiedFieldSelector;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.SpecifiedMethodSelector;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.ParameterBindersSource;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialMapper;
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
            TypeDescription origin, FieldDescription fieldDescription) throws ClassGeneratorException {
        return new ClassFactory.ShortcutBuilder<>(elementType)
                .addMethodSingleton(
                        elementType.getDeclaredMethods()[0],
                        resultMapper.getWrapperReturnType()
                )
                .setMemberSelector(new SpecifiedFieldSelector(fieldDescription))

                .setResultMapper(resultMapper)
                .endMethodDescription()
                .build();
    }

    @Override
    public ClassFactory<F> getMethodElement(
            TypeDescription origin, MethodDescription originMethod) throws ClassGeneratorException {
        return new ClassFactory.ShortcutBuilder<>(elementType)
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
,
    public static class AbstractBuilder<F, R, T extends AbstractBuilder<F, R, T>>
            implements ElementFactory.BuilderInterface<F> {

        private final Class<F> elementType;
        private final ResultMapperCollection<R> resultMapper;
        private ParameterBindersSource parameterMapper ;

        public AbstractBuilder(Class<F> elementType, Class<R> resultType) {
            this.elementType = elementType;
            this.resultMapper = new ResultMapperCollection<>(resultType);
        }

        @Override
        public ElementFactory<F> build() {
            return new GetterElementFactory<>(elementType, resultMapper, parameterMapper);
        }

        public T setElementFactory(ResultMapperCollection<R> resultMapper) {
            this.resultMapper = resultMapper;
            return (T) this;
        }

        public T setParameterMapper(ParameterBindersSource partialParameterUnion) {
            this.partialParameterUnion = partialParameterUnion;
            return (T) this;
        }

        public T addParameterTranslator(PartialMapper mapper) {
            parameterMapper.add(mapper);
            partialParameterUnion = partialParameterUnion.add(mapper);
            return (T) this;
        }
    }

    public static final class ShortcutBuilder<M extends AbstractMethodCollectionDescriptor.BuilderInterface<F>, F>
            extends AbstractBuilder<F, ShortcutBuilder<M, F>> {
        @Delegate
        private ElementFactoryBuilderParentReference<F, M> parentReference;

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
