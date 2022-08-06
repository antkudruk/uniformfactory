package com.github.antkudruk.uniformfactory.test.gradleplugin.setter;

import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class SetterTest {

    @Marker
    public static class Origin {
        @Marker
        public Integer value;
    }

    @Marker
    public static class OriginWithPrimitive {
        @Marker
        public int value;
    }

    @Test
    public void testIntegerField() throws ReflectiveOperationException {
        // given
        ClassFactoryGeneratorImpl classFactoryGenerator = new ClassFactoryGeneratorImpl();

        Origin origin = new Origin();

        Function<Origin, ? extends Adapter> classFactory
                = classFactoryGenerator.generateMetaClass(Origin.class);

        Adapter adapter = classFactory.apply(origin);

        // when
        adapter.setValue("10");

        // then
        int value = Whitebox.getInternalState(origin, "value");
        assertEquals(10, value);
    }

    @Test
    public void testPrimitiveField() throws ReflectiveOperationException {
        // given
        ClassFactoryGeneratorImpl classFactoryGenerator = new ClassFactoryGeneratorImpl();

        OriginWithPrimitive origin = new OriginWithPrimitive();

        Function<OriginWithPrimitive, ? extends Adapter> classFactory
                = classFactoryGenerator.generateMetaClass(OriginWithPrimitive.class);

        Adapter adapter = classFactory.apply(origin);

        // when
        adapter.setValue("10");

        // then
        int value = Whitebox.getInternalState(origin, "value");
        assertEquals(10, value);
    }
}
