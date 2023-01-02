package com.github.antkudruk.uniformfactory.base;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TypeShortcuts {

    public static final Map<Class<?>, Class<?>> UNBOXED_TYPE = Collections.unmodifiableMap(new HashMap<Class<?>, Class<?>>() {{
        put(int.class, Integer.class);
        put(byte.class, Byte.class);
        put(char.class, Character.class);
        put(long.class, Long.class);
        put(boolean.class, Boolean.class);
        put(double.class, Double.class);
        put(float.class, Float.class);
        put(void.class, Void.class);
    }});

    public static <E> Class<E> getBoxedType(Class<E> in) {
        //noinspection unchecked
        return (Class<E>) UNBOXED_TYPE.getOrDefault(in, in);
    }
}
