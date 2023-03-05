package com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist;

import org.junit.Test;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AdaptorGeneratingTest {

    private static String PARAMETER_VALUE = "123";
    private static Integer PARAMETER_INT_VALUE = 123;

    private ClassFactoryGeneratorImpl testSubject = new ClassFactoryGeneratorImpl();

    public AdaptorGeneratingTest() throws NoSuchMethodException {
    }

    @Test
    public void givenOrigin1_whenGenerateMetaClass_thenReturnWrapper() {
        // given
        Origin1 origin = mock(Origin1.class);

        // when
        Function<Origin1, ? extends Wrapper> meta = testSubject.generateMetaClass(Origin1.class);
        Wrapper w = meta.apply(origin);

        // then
        w.getProcessors().forEach(e -> e.process("test"));
        verify(origin, times(1)).process();
    }

    @Test
    public void givenOrigin2_whenGenerateMetaClass_thenReturnWrapper() {
        // given
        Function<String, String> callback1 = (Function<String, String>)mock(Function.class);
        when(callback1.apply(any())).thenReturn("");
        Function<Integer, Boolean> callback2 = (Function<Integer, Boolean>)mock(Function.class);
        when(callback2.apply(eq(PARAMETER_INT_VALUE))).thenReturn(true);
        Origin2 origin = new Origin2(callback1, callback2);

        // when
        Function<Origin2, ? extends Wrapper> meta = testSubject.generateMetaClass(Origin2.class);
        Wrapper w = meta.apply(origin);

        // then
        w.getProcessors().forEach(e -> e.process(PARAMETER_VALUE));
        verify(callback1, times(1)).apply(eq(PARAMETER_VALUE));
        verify(callback2, times(1)).apply(eq(PARAMETER_INT_VALUE));
    }
}
