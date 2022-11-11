package com.github.antkudruk.uniformfactory.singleton.argument.typemapper;

import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class SuperParameterTranslatorTest {

    public static class Foo {

    }

    public static class Bar extends Foo {

    }

    public static class Baz extends Bar {

    }

    @SuppressWarnings("unchecked")
    private final Function<Bar, ?> translator = (Function<Bar, ?>)mock(Function.class);

    private final SuperParameterTranslator<Bar> tested = new SuperParameterTranslator<>(Bar.class, translator);

    @Test
    public void givenSameClass_whenIsApplicable_thenFalse() {
        // when/then
        assertTrue(tested.isApplicable(new TypeDescription.ForLoadedType(Bar.class)));
    }

    @Test
    public void givenSubClass_whenIsApplicable_thenFalse() {
        // when/then
        assertFalse(tested.isApplicable(new TypeDescription.ForLoadedType(Baz.class)));
    }

    @Test
    public void givenSuperClass_whenIsApplicable_thenTrue() {
        // when/then
        assertTrue(tested.isApplicable(new TypeDescription.ForLoadedType(Foo.class)));
    }
}
