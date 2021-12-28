package com.github.antkudruk.uniformfactory.test.gradleplugin.setter.domain;

import com.github.antkudruk.uniformfactory.test.gradleplugin.setter.Marker;

@Marker
public class OriginImpl {
    @Marker
    public Integer value;

    public OriginImpl(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public OriginImpl setValue(Integer value) {
        this.value = value;
        return this;
    }
}
