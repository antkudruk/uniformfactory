package com.github.antkudruk.uniformfactory.test.gradleplugin.emptywrapper.interfaceinherited;

import org.junit.Test;

public class EmptyTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    public void test() {
        OriginImpl origin = new OriginImpl();
        assert Wrapper.class.isAssignableFrom(origin.getWrapper().getClass());
    }
}
