package com.github.antkudruk.uniformfactory.test.gradleplugin.wrapperclass;

import com.github.antkudruk.uniformfactory.test.gradleplugin.wrapperclass.domain.OriginImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClassBasedWrapperTest {
    @Test
    public void test() {
        OriginImpl originImpl = new OriginImpl();
        Origin origin = (Origin)originImpl;

        assertEquals(2, origin.getWrapper().getAccumulated());
        assertEquals(4, origin.getWrapper().getAccumulated());
        assertEquals(6, origin.getWrapper().getAccumulated());
    }
}
