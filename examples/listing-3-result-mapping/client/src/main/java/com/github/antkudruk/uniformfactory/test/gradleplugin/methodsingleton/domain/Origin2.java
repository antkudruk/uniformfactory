package com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.domain;

import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.Identity;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.Marker;

@Marker
public class Origin2 {
    @Identity
    public String getName() {
        return "name";
    }
}
