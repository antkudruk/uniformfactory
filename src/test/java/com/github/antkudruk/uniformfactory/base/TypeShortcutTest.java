package com.github.antkudruk.uniformfactory.base;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TypeShortcutTest {

    @Test
    public void testChar() {
        testOneTypeMapping(char.class, Character.class);
    }

    @Test
    public void testByte() {
        testOneTypeMapping(byte.class, Byte.class);
    }

    @Test
    public void testInt() {
        testOneTypeMapping(int.class, Integer.class);
    }

    @Test
    public void testLong() {
        testOneTypeMapping(long.class, Long.class);
    }

    @Test
    public void testBool() {
        testOneTypeMapping(boolean.class, Boolean.class);
    }

    @Test
    public void testDouble() {
        testOneTypeMapping(double.class, Double.class);
    }

    @Test
    public void testFloat() {
        testOneTypeMapping(float.class, Float.class);
    }

    @Test
    public void testCustom() {
        testOneTypeMapping(TypeShortcutTest.class, TypeShortcutTest.class);
    }

    @Test
    public void testVoid() {
        testOneTypeMapping(void.class, Void.class);
    }

    private void testOneTypeMapping(Class<?> unboxed, Class<?> boxed) {
        assertEquals(boxed, TypeShortcuts.getBoxedType(unboxed));
    }
}
