package com.github.antkudruk.uniformfactory.test.gradleplugin.emptywrapper.interfacebased;

import org.junit.Test;

import static org.junit.Assert.assertNull;

public class EmptyTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    public void test() {
        OriginImpl origin = new OriginImpl();
        assert Wrapper.class.isAssignableFrom(origin.getWrapper().getClass());
    }

    @Test
    public void testExplicitOriginImpl () {
        ExplicitOriginImpl origin = new ExplicitOriginImpl();
        assertNull(origin.getWrapper());
    }
}
