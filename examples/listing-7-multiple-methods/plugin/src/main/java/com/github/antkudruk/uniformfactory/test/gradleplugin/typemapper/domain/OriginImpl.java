package com.github.antkudruk.uniformfactory.test.gradleplugin.typemapper.domain;

import com.github.antkudruk.uniformfactory.test.gradleplugin.typemapper.FirstMethodMarker;
import com.github.antkudruk.uniformfactory.test.gradleplugin.typemapper.Marker;
import com.github.antkudruk.uniformfactory.test.gradleplugin.typemapper.SecondMethodMarker;

// TODO: Move outside
@Marker
public class OriginImpl {
    @FirstMethodMarker
    public int process(String value) {
        return value.length();
    }

    @SecondMethodMarker
    public Long seconfField = 100L;
}
