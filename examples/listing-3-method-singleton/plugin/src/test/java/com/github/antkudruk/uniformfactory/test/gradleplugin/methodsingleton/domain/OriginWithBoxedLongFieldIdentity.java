package com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.domain;

import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.Identity;

public class OriginWithBoxedLongFieldIdentity {
    @Identity
    private Long id = 10L;
}
