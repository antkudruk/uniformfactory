package com.github.antkudruk.uniformfactory.methodcollection;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.SpecifiedFieldSelector;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.SpecifiedMethodSelector;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.ParameterBindersSource;
import com.github.antkudruk.uniformfactory.singleton.descriptors.ResultMapperCollection;
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
}
