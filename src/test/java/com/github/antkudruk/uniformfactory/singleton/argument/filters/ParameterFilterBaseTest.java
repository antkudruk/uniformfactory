package com.github.antkudruk.uniformfactory.singleton.argument.filters;

import org.powermock.api.mockito.PowerMockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class ParameterFilterBaseTest {
    ParameterFilter mockParameterFilter(Boolean expected) {
        ParameterFilter mock = PowerMockito.mock(ParameterFilter.class);

        PowerMockito
                .when(mock.useArgument(any(), eq(0)))
                .thenReturn(expected);

        return mock;
    }
}
