package com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.origins;

import com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.First;
import com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.MethodMarker;
import com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.HasWrapper;
import com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.Second;

import java.util.Objects;

@HasWrapper
public class OriginTakesStringAndSecondReturningBoolean {

    @SuppressWarnings("unused")
    @MethodMarker
    public Boolean concat(@First String first, @Second String second) {
        return Objects.equals(first, second);
    }
}
