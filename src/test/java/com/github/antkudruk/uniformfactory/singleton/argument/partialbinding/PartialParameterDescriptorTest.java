package com.github.antkudruk.uniformfactory.singleton.argument.partialbinding;

import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MethodCall.class})
@Ignore
public class PartialParameterDescriptorTest {

    private static final int PARAMETER_INDEX = 0;

    public static class WrapperParameter {

    }

    public static class OriginParameter {

    }

    @Test
    public void getSourceTest() {

        PartialParameterDescriptor<?, ?> partialParameterDescriptor
                = new PartialParameterDescriptor<>(
                0, 0, t -> t);

        addWithAssertions(partialParameterDescriptor);

        assertEquals(PartialParameterDescriptor.class,
                partialParameterDescriptor.getClass());
    }

    private void addWithAssertions(PartialDescriptor partialDescriptor) {

        MethodCall.WithoutSpecifiedTarget particularMethodCall =
                Mockito.mock(MethodCall.WithoutSpecifiedTarget.class);
        Mockito
                .when(particularMethodCall.onField(anyString()))
                .thenReturn(particularMethodCall);

        Mockito
                .when(particularMethodCall.withArgument(PARAMETER_INDEX))
                .thenReturn(particularMethodCall);

        Mockito
                .when(particularMethodCall.withAssigner(
                        eq(Assigner.DEFAULT), eq(Assigner.Typing.DYNAMIC)))
                .thenReturn(particularMethodCall);

        PowerMockito.mockStatic(MethodCall.class);
        PowerMockito
                .when(MethodCall.invoke(any(Method.class)))
                .thenReturn(particularMethodCall);

        MethodCall methodCall = Mockito.mock(MethodCall.class);
        Mockito.when(methodCall.withMethodCall(any(MethodCall.class)))
                .thenReturn(methodCall);

        partialDescriptor.addWith(methodCall);

        Mockito.verify(methodCall, times(1))
                .withMethodCall(eq(particularMethodCall));

        Mockito.verify(particularMethodCall, times(1))
                .onField(anyString());

        Mockito.verify(particularMethodCall, times(1))
                .withArgument(PARAMETER_INDEX);

        Mockito.verify(particularMethodCall, times(1))
                .withAssigner(eq(Assigner.DEFAULT), eq(Assigner.Typing.DYNAMIC));
    }
}
