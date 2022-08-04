package com.github.antkudruk.uniformfactory.test.gradleplugin.setter;

import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class SetterTest {

    private static final String CSS_MARGIN = "margin";
    private static final String CSS_PADDING = "padding";

    @SuppressWarnings("unused")
    @Marker
    public static class Origin {
        public String backgroundColor;

        @Css(CSS_MARGIN)
        public Integer margin;

        @Css(CSS_PADDING)
        private String padding;
    }

    @Test
    public void test() throws ReflectiveOperationException {
        // given
        ClassFactoryGeneratorImpl classFactoryGenerator = new ClassFactoryGeneratorImpl();

        Origin origin = new Origin();

        Function<Origin, ? extends Adapter> classFactory
                = classFactoryGenerator.generateMetaClass(Origin.class);

        Adapter adapter = classFactory.apply(origin);

        // when
        adapter.setters().get(CSS_MARGIN).setValue("10");
        adapter.setters().get(CSS_PADDING).setValue("12px");

        // then
        assertEquals(Integer.valueOf(10), Whitebox.getInternalState(origin, "margin"));
        assertEquals("12px", Whitebox.getInternalState(origin, "padding"));
    }
}
