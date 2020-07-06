package com.github.antkudruk.uniformfactory.singleton.argument.partialbinding;

import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.ValueSource;
import com.github.antkudruk.uniformfactory.singleton.argument.filters.ParameterFilter;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;

public class PartialMapperImplTest {

    private static final int ORIGIN_INDEX = 1;

    @Test
    public void getArgumentBindersTest() {

        TypeDescription originParameterType = PowerMockito.mock(TypeDescription.class);

        PartialDescriptor resultPartialDescriptor
                = PowerMockito.mock(PartialDescriptor.class);

        ValueSource valueSource = mockValueSource(originParameterType,
                resultPartialDescriptor);

        ParameterFilter parameterFilter = mockParameterFilter();

        MethodDescription methodDescription = mockMethodDescription(
                mockParameterList(
                        PowerMockito.mock(ParameterDescription.class),
                        mockParameter(originParameterType)));

        List<PartialDescriptor> result
                = new PartialMapperImpl(parameterFilter, valueSource)
                .getArgumentBinders(methodDescription);
        assertEquals(resultPartialDescriptor, result.get(0));
    }

    private ParameterDescription mockParameter(TypeDescription parameterType) {
        ParameterDescription originParameter = PowerMockito.mock(ParameterDescription.class,
                Mockito.RETURNS_DEEP_STUBS);

        PowerMockito.when(originParameter.getType().asErasure())
                .thenReturn(parameterType);

        return originParameter;
    }

    private ParameterFilter mockParameterFilter() {
        ParameterFilter parameterFilter = PowerMockito.mock(ParameterFilter.class);
        PowerMockito.when(parameterFilter.useArgument(
                any(), eq(ORIGIN_INDEX))).thenReturn(true);
        return parameterFilter;
    }

    private ValueSource mockValueSource(TypeDescription originParameterType,
                                        PartialDescriptor resultPartialDescriptor) {
        ValueSource valueSource = PowerMockito.mock(ValueSource.class);
        PowerMockito.when(valueSource.getSource(
                eq(ORIGIN_INDEX),
                eq(originParameterType)
        )).thenReturn(Optional.of(resultPartialDescriptor));
        return valueSource;
    }

    @SuppressWarnings("unchecked")
    private MethodDescription mockMethodDescription(ParameterList parameterList) {
        MethodDescription methodDescription = PowerMockito.mock(MethodDescription.class);
        PowerMockito
                .when(methodDescription.getParameters())
                .thenReturn(parameterList);
        return methodDescription;
    }

    private ParameterList mockParameterList(ParameterDescription... parameterDescriptions) {
        ParameterList parameterList = PowerMockito.mock(ParameterList.class);
        PowerMockito.when(parameterList.size()).thenReturn(parameterDescriptions.length);
        for (int i = 0; i < parameterDescriptions.length; i++) {
            PowerMockito.when(parameterList.get(i)).thenReturn(parameterDescriptions[i]);
        }
        return parameterList;
    }

    @Test
    public void getArgumentBindersWithErrorTest() {

        ValueSource valueSource = Mockito.mock(ValueSource.class);
        Mockito.when(valueSource.getSource(anyInt(), any(TypeDescription.class)))
                .thenReturn(Optional.empty());

        ParameterFilter parameterFilter = mockParameterFilter();

        MethodDescription methodDescription = mockMethodDescription(
                mockParameterList(
                        PowerMockito.mock(ParameterDescription.class),
                        mockParameter(PowerMockito.mock(TypeDescription.class))));

        List<PartialDescriptor> argumants = new PartialMapperImpl(parameterFilter, valueSource)
                .getArgumentBinders(methodDescription);

        assertEquals(0, argumants.size());
    }
}
