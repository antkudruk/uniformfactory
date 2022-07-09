package com.github.antkudruk.uniformfactory.methodcollection;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.SpecifiedFieldSelector;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.ParameterBindersSource;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialMapper;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;

public class SetterElementFactory<F, R> implements ElementFactory<F> {

    private final Class<F> elementType;
    private final PartialMapper parameterMapper;

    public SetterElementFactory(
            Class<F> elementType,
            PartialMapper parameterMapper) {

        this.elementType = elementType;
        this.parameterMapper = parameterMapper;
    }

    @Override
    public ClassFactory<F> getFieldElement(
            TypeDescription origin,
            FieldDescription fieldDescription) throws ClassGeneratorException {
        return new ClassFactory.ShortcutBuilder<>(elementType)
                .addSetter(
                        elementType.getDeclaredMethods()[0],
                        // TODO: Consider arbitrary argumrnt number, or even getting rid of a field type here
                        elementType.getDeclaredMethods()[0].getParameterTypes()[0]
                )
                .setMemberSelector(new SpecifiedFieldSelector(fieldDescription))
                .endMethodDescription()
                .build();
    }

    @Override
    public ClassFactory<F> getMethodElement(
            TypeDescription origin,
            MethodDescription methodDescription) throws ClassGeneratorException {
        // TODO add default implementation
        // TODO: Test the case where result is not void (has to be ignored)
        throw new RuntimeException("");
    }
}
