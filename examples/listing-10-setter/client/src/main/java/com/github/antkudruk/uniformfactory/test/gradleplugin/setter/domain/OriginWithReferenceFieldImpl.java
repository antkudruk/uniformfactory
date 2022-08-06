package com.github.antkudruk.uniformfactory.test.gradleplugin.setter.domain;

import com.github.antkudruk.uniformfactory.test.gradleplugin.setter.Marker;

@Marker
public class OriginWithReferenceFieldImpl {
    @Marker
    public Integer value;

    public Integer getValue() {
        return value;
    }
}
