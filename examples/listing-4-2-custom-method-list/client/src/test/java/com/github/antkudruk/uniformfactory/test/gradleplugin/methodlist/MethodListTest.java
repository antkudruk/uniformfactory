package com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist;

import com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist.CallableObjectsRegistry;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist.domain.Origin1;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist.domain.Origin2;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist.Origin;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.function.Function;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class MethodListTest {

    private static final Integer EVENT_TYPE_INT = 100500;
    private static final String EVENT_TYPE_STRING = EVENT_TYPE_INT.toString();

    @Test
    public void everythingReturnsTrue () {
        // given
        Runnable runnable = mock(Runnable.class);
        Function<String, String> consumerString = mock(Function.class);
        when(consumerString.apply(eq(EVENT_TYPE_STRING))).thenReturn("yes");

        Function<Integer, Boolean> consumerInteger = mock(Function.class);
        when(consumerInteger.apply(eq(EVENT_TYPE_INT))).thenReturn(true);

        // when
        new Origin1(runnable);
        new Origin2(consumerString, consumerInteger);
        boolean result = CallableObjectsRegistry.INSTANCE.call(EVENT_TYPE_STRING);

        // then
        assertTrue(result);
        verify(runnable, times(1)).run();
        verify(consumerString, times(1)).apply(eq(EVENT_TYPE_STRING));
        verify(consumerInteger, times(1)).apply(eq(EVENT_TYPE_INT));
    }

    @Test
    public void everythingSecondFalse () {
        Runnable runnable = mock(Runnable.class);
        Function<String, String> consumerString = mock(Function.class);
        when(consumerString.apply(eq(EVENT_TYPE_STRING))).thenReturn("no");

        Function<Integer, Boolean> consumerInteger = mock(Function.class);
        when(consumerInteger.apply(eq(EVENT_TYPE_INT))).thenReturn(true);

        new Origin1(runnable);
        new Origin2(consumerString, consumerInteger);

        assertFalse(CallableObjectsRegistry.INSTANCE.call(EVENT_TYPE_STRING));

        verify(runnable, times(1)).run();
        verify(consumerString, times(1)).apply(eq(EVENT_TYPE_STRING));
        verify(consumerInteger, times(1)).apply(eq(EVENT_TYPE_INT));
    }

    @Test
    public void everythingThirdFalse () {
        Runnable runnable = mock(Runnable.class);
        Function<String, String> consumerString = mock(Function.class);
        when(consumerString.apply(eq(EVENT_TYPE_STRING))).thenReturn("yes");

        Function<Integer, Boolean> consumerInteger = mock(Function.class);
        when(consumerInteger.apply(eq(EVENT_TYPE_INT))).thenReturn(false);

        Origin1 origin1 = new Origin1(runnable);
        new Origin2(consumerString, consumerInteger);

        assertFalse(CallableObjectsRegistry.INSTANCE.call(EVENT_TYPE_STRING));

        verify(runnable, times(1)).run();
        verify(consumerString, times(1)).apply(eq(EVENT_TYPE_STRING));
        verify(consumerInteger, times(1)).apply(eq(EVENT_TYPE_INT));

        ((Origin)origin1)
                .getWrapper()
                .getDescriptors()
                .forEach(i -> i.set("10"));
        assertEquals(10, origin1.getFontSize());
        assertEquals(10L, origin1.getWidth().longValue());
        assertEquals("10", origin1.getFontName());
    }
}

