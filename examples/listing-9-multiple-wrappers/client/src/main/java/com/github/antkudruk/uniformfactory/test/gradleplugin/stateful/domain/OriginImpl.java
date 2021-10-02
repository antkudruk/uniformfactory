package com.github.antkudruk.uniformfactory.test.gradleplugin.stateful.domain;

import com.github.antkudruk.uniformfactory.test.gradleplugin.stateful.Marker;

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
