package com.github.antkudruk.uniformfactory.singleton.argument.valuesource;

import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialDescriptor;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class ParameterValueTest {

    private static final int WRAPPER_INDEX = 20;
    private static final int ORIGIN_INDEX = 10;

    public static class WrapperParameter {

    }

    public static class OriginParameter {

    }

    @Test
    public void whenGetSource_thenReturnPartialParameterDescriptor() {
        // given
        TypeDescription originParameterTypeDescription
                = new TypeDescription.ForLoadedType(OriginParameter.class);

        ParameterValue<WrapperParameter> parameterValue = new ParameterValue<>(
                WrapperParameter.class, WRAPPER_INDEX);

        Function<WrapperParameter, OriginParameter> mapper = t -> new OriginParameter();

        parameterValue = parameterValue.addTranslatorForExtends(
                originParameterTypeDescription, mapper);

        // when
        PartialDescriptor parameterDescriptor = parameterValue
                .getSource(ORIGIN_INDEX, originParameterTypeDescription)
                .orElseThrow(RuntimeException::new);

        // then
        assertEquals(
                ORIGIN_INDEX,
                (int)Whitebox.getInternalState(parameterDescriptor, "originIndex"));

        assertEquals(
                WRAPPER_INDEX,
                (int)Whitebox.getInternalState(parameterDescriptor, "wrapperIndex"));

        assertEquals(
                mapper,
                Whitebox.getInternalState(parameterDescriptor, "parameterTranslator"));
    }
}
