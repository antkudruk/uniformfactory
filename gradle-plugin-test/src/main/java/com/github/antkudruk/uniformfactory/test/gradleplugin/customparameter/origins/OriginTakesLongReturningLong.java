package com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.origins;

import com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.HasWrapper;
import com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.MethodMarker;
import com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.Second;

@HasWrapper
public class OriginTakesLongReturningLong {

    @SuppressWarnings("unused")
    @MethodMarker
    public Long concat(@Second Long s1) {
        return s1;
    }
}
