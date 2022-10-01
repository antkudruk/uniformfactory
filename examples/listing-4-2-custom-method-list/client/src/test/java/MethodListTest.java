import com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist.CallableObjectsRegistry;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist.domain.Origin1;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist.domain.Origin2;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.function.Function;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

@SuppressWarnings("unchecked")
public class MethodListTest {

    private static final Integer EVENT_TYPE_INT = 100500;
    private static final String EVENT_TYPE_STRING = EVENT_TYPE_INT.toString();

    @Test
    public void everythingReturnsTrue () {
        Runnable runnable = Mockito.mock(Runnable.class);
        Function<String, String> consumerString = Mockito.mock(Function.class);
        Mockito.when(consumerString.apply(eq(EVENT_TYPE_STRING))).thenReturn("yes");

        Function<Integer, Boolean> consumerInteger = Mockito.mock(Function.class);
        Mockito.when(consumerInteger.apply(eq(EVENT_TYPE_INT))).thenReturn(true);

        new Origin1(runnable);
        new Origin2(consumerString, consumerInteger);

        assertTrue(CallableObjectsRegistry.INSTANCE.call(EVENT_TYPE_STRING));

        Mockito.verify(runnable, times(1)).run();
        Mockito.verify(consumerString, times(1)).apply(eq(EVENT_TYPE_STRING));
        Mockito.verify(consumerInteger, times(1)).apply(eq(EVENT_TYPE_INT));
    }

    @Test
    public void everythingSecondFalse () {
        Runnable runnable = Mockito.mock(Runnable.class);
        Function<String, String> consumerString = Mockito.mock(Function.class);
        Mockito.when(consumerString.apply(eq(EVENT_TYPE_STRING))).thenReturn("no");

        Function<Integer, Boolean> consumerInteger = Mockito.mock(Function.class);
        Mockito.when(consumerInteger.apply(eq(EVENT_TYPE_INT))).thenReturn(true);

        new Origin1(runnable);
        new Origin2(consumerString, consumerInteger);

        assertFalse(CallableObjectsRegistry.INSTANCE.call(EVENT_TYPE_STRING));

        Mockito.verify(runnable, times(1)).run();
        Mockito.verify(consumerString, times(1)).apply(eq(EVENT_TYPE_STRING));
        Mockito.verify(consumerInteger, times(1)).apply(eq(EVENT_TYPE_INT));
    }

    @Test
    public void everythingThirdFalse () {
        Runnable runnable = Mockito.mock(Runnable.class);
        Function<String, String> consumerString = Mockito.mock(Function.class);
        Mockito.when(consumerString.apply(eq(EVENT_TYPE_STRING))).thenReturn("yes");

        Function<Integer, Boolean> consumerInteger = Mockito.mock(Function.class);
        Mockito.when(consumerInteger.apply(eq(EVENT_TYPE_INT))).thenReturn(false);

        new Origin1(runnable);
        new Origin2(consumerString, consumerInteger);

        assertFalse(CallableObjectsRegistry.INSTANCE.call(EVENT_TYPE_STRING));

        Mockito.verify(runnable, times(1)).run();
        Mockito.verify(consumerString, times(1)).apply(eq(EVENT_TYPE_STRING));
        Mockito.verify(consumerInteger, times(1)).apply(eq(EVENT_TYPE_INT));
    }
}

