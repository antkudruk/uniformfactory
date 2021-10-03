package com.github.antkudruk.uniformfactory.test.gradleplugin.methodmap.domain;

import com.github.antkudruk.uniformfactory.test.gradleplugin.methodmap.CoordinateMarker;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodmap.Marker;

@Marker
public class PointTypeA {
    @CoordinateMarker("x")
    private int positionX = 10;

    @CoordinateMarker("y")
    public String getPositionY() {
        return "20";
    }

    @CoordinateMarker("z")
    public Boolean getPositionZ() {
        return true;
    }
}
