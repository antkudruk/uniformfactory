package com.github.antkudruk.uniformfactory.test.gradleplugin.empty;

import com.github.antkudruk.uniformfactory.test.gradleplugin.empty.origins.OriginImpl;
import org.junit.Test;

public class EmptyTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    public void test() {
        OriginImpl origin = new OriginImpl();
        assert ClassFactoryGeneratorImpl.Wrapper.class.isAssignableFrom(((ClassFactoryGeneratorImpl.Origin)origin).getWrapper().getClass());
    }
}
