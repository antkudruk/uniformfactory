package com.github.antkudruk.uniformfactory.test.gradleplugin.setter;

import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class SetterTest {

    private static final String CSS_MARGIN = "margin";
    private static final String CSS_PADDING = "padding";
    private static final String CSS_BACKGROUND_COLOR = "background-color";

    @Marker
    public static class Origin {
        public String backgroundColor;

        @Css(CSS_MARGIN)
        public Integer margin;

        @Css(CSS_PADDING)
        private String padding;

        /* TODO: Add an opportunity to skip it
        @Css(CSS_BACKGROUND_COLOR)
        public void setBackgroundColor(String backgroundColor) {
            this.backgroundColor = backgroundColor;
        }
        */
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
        Map<String, Fun> map = adapter.setters();
        adapter.setters().get(CSS_MARGIN).setValue("10");
        adapter.setters().get(CSS_PADDING).setValue("12");
        //adapter.setters().get(CSS_BACKGROUND_COLOR).setValue("red");

        // then
        Whitebox.getInternalState(origin, "value");
        assertEquals(Integer.valueOf(10), Whitebox.getInternalState(origin, "margin"));
        assertEquals(Integer.valueOf(12), Whitebox.getInternalState(origin, "padding"));
        //assertEquals("red", Whitebox.getInternalState(origin, "backgroundColor"));
    }
}
