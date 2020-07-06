package com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter;

import com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.origins.OriginConcatStringAndBooleanReturningString;
import com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.origins.OriginConcatStringAndLongReturningString;
import com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.origins.OriginConcatTwoStringsReturningString;
import com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.origins.OriginTakesLongReturningLong;
import com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.origins.OriginTakesStringAndSecondReturningBoolean;
import com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.wrapper.Origin;
import junit.framework.TestCase;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class CustomParameterTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testOriginConcatTwoStringsReturningString () {
        OriginConcatTwoStringsReturningString origin = new OriginConcatTwoStringsReturningString();
        TestCase.assertEquals("Foo Bar", ((Origin)origin).getWrapper().common("Foo", "Bar"));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testOriginConcatStringAndLongReturningString () {
        OriginConcatStringAndLongReturningString origin = new OriginConcatStringAndLongReturningString();
        assertEquals("Foo 100", ((Origin)origin).getWrapper().common("Foo", "100"));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testOriginConcatStringAndBooleanReturningString () {
        OriginConcatStringAndBooleanReturningString origin = new OriginConcatStringAndBooleanReturningString();
        assertEquals("Foo false", ((Origin)origin).getWrapper().common("Foo", "Bar"));
    }


    @SuppressWarnings("ConstantConditions")
    @Test
    public void testOriginTakesLongReturningLong () {
        OriginTakesLongReturningLong origin = new OriginTakesLongReturningLong();
        assertEquals("200 units", ((Origin)origin).getWrapper().common("Foo", "200"));
    }


    @SuppressWarnings("ConstantConditions")
    @Test
    public void testOriginTakesStringAndSecondReturningBoolean () {
        OriginTakesStringAndSecondReturningBoolean origin = new OriginTakesStringAndSecondReturningBoolean();
        assertEquals("Yes", ((Origin)origin).getWrapper().common("Foo", "Foo"));
        assertEquals("No", ((Origin)origin).getWrapper().common("Foo", "Bar"));
    }
}
