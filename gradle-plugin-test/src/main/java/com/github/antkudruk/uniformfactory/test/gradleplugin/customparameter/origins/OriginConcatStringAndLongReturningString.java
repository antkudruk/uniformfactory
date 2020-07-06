package com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.origins;

import com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.First;
import com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.MethodMarker;
import com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.Second;
import com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.HasWrapper;

@HasWrapper
public class OriginConcatStringAndLongReturningString {
    @SuppressWarnings("unused")
    @MethodMarker
    public String concat(@First String s0, @Second Long s1) {
        return s0 + " " + s1;
    }
}
