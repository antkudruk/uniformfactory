package com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CallableObjectsRegistryTest {

    @Test
    public void whenCall_thenObjetsCalled() {
        // given
        CallableObjectsRegistry testSubject = new CallableObjectsRegistry();
        Processor processor = mock(Processor.class);
        when(processor.process(eq("eventName"))).thenReturn(true);
        Wrapper wrapper = mock(Wrapper.class);
        when(wrapper.getProcessors()).thenReturn(Collections.singletonList(processor));
        testSubject.addObject(wrapper);

        // when
        boolean result = testSubject.call("eventName");

        // then
        assertTrue(result);
    }
}
