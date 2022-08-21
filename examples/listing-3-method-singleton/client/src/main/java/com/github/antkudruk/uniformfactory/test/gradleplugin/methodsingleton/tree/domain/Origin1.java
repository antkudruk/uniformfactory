package com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.tree.domain;

import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.Identity;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.Marker;

@Marker
public class Origin1 {
    @Identity
    private Long number = 10L;
}
