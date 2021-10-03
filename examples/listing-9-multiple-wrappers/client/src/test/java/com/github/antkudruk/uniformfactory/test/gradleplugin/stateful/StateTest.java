package com.github.antkudruk.uniformfactory.test.gradleplugin.stateful;

import com.github.antkudruk.uniformfactory.test.gradleplugin.stateful.domain.OriginImpl;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;

public class StateTest {
    @Test
    public void test() {
        OriginImpl originImpl = new OriginImpl(1);

        Origin origin = (Origin) originImpl;

        assertNull(origin.getState().getId());

        originImpl.setValue(2);

        assertEquals(1, origin.getState().getId().intValue());
        assertEquals(2, origin.getState().getId().intValue());
    }
}
