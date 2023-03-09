package com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.domain;

import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.Identity;

public class OriginWithBoxedLongMethodIdentity {
    @Identity
    public Long getId() {
        return 30L;
    }
}
