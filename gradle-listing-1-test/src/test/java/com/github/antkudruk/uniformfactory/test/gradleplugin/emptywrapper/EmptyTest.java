package com.github.antkudruk.uniformfactory.test.gradleplugin.emptywrapper;

import org.junit.Test;

public class EmptyTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    public void test() {
        OriginImpl origin = new OriginImpl();
        assert Wrapper.class.isAssignableFrom(((Origin)origin).getWrapper().getClass());
    }
}
