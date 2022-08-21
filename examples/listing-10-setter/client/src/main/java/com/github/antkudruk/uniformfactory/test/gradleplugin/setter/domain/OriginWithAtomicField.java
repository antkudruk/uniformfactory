package com.github.antkudruk.uniformfactory.test.gradleplugin.setter.domain;

import com.github.antkudruk.uniformfactory.test.gradleplugin.setter.Marker;

@Marker
public class OriginWithAtomicField {
    @Marker
    public int value;

    public Integer getValue() {
        return value;
    }
}
