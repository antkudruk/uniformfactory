package com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist;

import org.junit.Test;

import java.util.function.Function;

public class AdaptorGeneratingTest {
    @Test
    public void test() throws NoSuchMethodException {
        Origin1 origin1 = new Origin1();
        ClassFactoryGeneratorImpl tested = new ClassFactoryGeneratorImpl();
        Function<Origin1, ? extends Wrapper> meta =  tested.generateMetaClass(Origin1.class);
        Wrapper w =  meta.apply(origin1);
        w.getProcessors();
    }
}
