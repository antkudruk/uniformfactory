package com.github.antkudruk.uniformfactory.singleton.argument.valuesource;

import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialParameterDescriptor;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PartialParameterDescriptor.class, ParameterValue.class})
@Ignore
public class ParameterValueTest {

    private static final int WRAPPER_INDEX = 20;
    private static final int ORIGIN_INDEX = 10;

    public static class WrapperParameter {

    }

    public static class OriginParameter {

    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void getSourceTest() throws Exception {

        PartialParameterDescriptor parameterDescriptor
                = PowerMockito.mock(PartialParameterDescriptor.class);

        PowerMockito
                .whenNew(PartialParameterDescriptor.class)
                .withAnyArguments()
                .thenReturn(parameterDescriptor);

        TypeDescription originParameterTypeDescription
                = new TypeDescription.ForLoadedType(OriginParameter.class);

        ParameterValue<WrapperParameter> parameterValue = new ParameterValue<>(
                WrapperParameter.class, WRAPPER_INDEX);

        Function<WrapperParameter, OriginParameter> mapper = t -> new OriginParameter();

        parameterValue = parameterValue.addTranslator(
                originParameterTypeDescription, mapper);

        Assert.assertEquals(
                parameterDescriptor,
                parameterValue.getSource(ORIGIN_INDEX, originParameterTypeDescription).get()
        );

        PowerMockito.verifyNew(PartialParameterDescriptor.class)
                .withArguments(eq(ORIGIN_INDEX), eq(WRAPPER_INDEX), eq(mapper));
    }
}
