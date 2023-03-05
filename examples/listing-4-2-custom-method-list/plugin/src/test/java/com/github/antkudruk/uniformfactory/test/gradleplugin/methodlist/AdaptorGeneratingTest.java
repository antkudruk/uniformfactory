package com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class AdaptorGeneratingTest {

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
        w.getProcessors().forEach(Object::notify);
        Mockito.verify(origin, times(1)).process();
    }

    @Test
    public void givenOrigin2_whenGenerateMetaClass_thenReturnWrapper() {
        // given
        Function callback1 = mock(Function.class);
        when(callback1.apply(any())).thenReturn("");
        Function callback2 = mock(Function.class);
        when(callback2.apply(any())).thenReturn(true);

        Origin2 origin = new Origin2(
                i -> i,
                i -> i > 0
        );

        // when
        Function<Origin2, ? extends Wrapper> meta = testSubject.generateMetaClass(Origin2.class);
        Wrapper w = meta.apply(origin);

        // then
        w.getProcessors().forEach(Object::notify);
        Mockito.verify(callback1, times(1)).apply(any());
        Mockito.verify(callback2, times(1)).apply(any());
    }
}
