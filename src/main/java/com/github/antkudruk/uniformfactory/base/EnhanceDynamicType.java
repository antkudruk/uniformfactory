package com.github.antkudruk.uniformfactory.base;

import net.bytebuddy.dynamic.DynamicType;

public interface EnhanceDynamicType<E> {
    DynamicType.Builder<E> apply(DynamicType.Builder<E> value);
}
