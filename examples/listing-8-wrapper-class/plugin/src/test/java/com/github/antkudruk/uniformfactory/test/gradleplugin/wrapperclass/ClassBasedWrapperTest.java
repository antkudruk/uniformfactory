package com.github.antkudruk.uniformfactory.test.gradleplugin.wrapperclass;

import com.github.antkudruk.uniformfactory.test.gradleplugin.wrapperclass.domain.OriginImpl;
import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class ClassBasedWrapperTest {
    @Test
    public void test() throws NoSuchMethodException {
        OriginImpl originImpl = new OriginImpl();
        ClassFactoryGeneratorImpl classFactoryGenerator
                = new ClassFactoryGeneratorImpl();
        Function<OriginImpl, ? extends Wrapper> metaClass
                = classFactoryGenerator.generateMetaClass(OriginImpl.class);

        Wrapper wrapper = metaClass.apply(originImpl);
        assertEquals(2, wrapper.getAccumulated());
        assertEquals(4, wrapper.getAccumulated());
        assertEquals(6, wrapper.getAccumulated());
    }
}
