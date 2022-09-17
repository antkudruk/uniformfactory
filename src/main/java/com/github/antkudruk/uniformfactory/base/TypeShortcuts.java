package com.github.antkudruk.uniformfactory.base;

import java.util.HashMap;
import java.util.Map;

public class TypeShortcuts {
    public static Class<?> getBoxedType(Class<?> in) {
        Map<Class<?>, Class<?>> unboxedTypes = new HashMap<>();
        unboxedTypes.put(int.class, Integer.class);
        unboxedTypes.put(byte.class, Byte.class);
        unboxedTypes.put(char.class, Character.class);
        unboxedTypes.put(long.class, Long.class);
        unboxedTypes.put(boolean.class, Boolean.class);
        unboxedTypes.put(double.class, Double.class);
        unboxedTypes.put(float.class, Float.class);
        unboxedTypes.put(void.class, Void.class);
        return unboxedTypes.getOrDefault(in, in);
    }
}
