package com.github.antkudruk.uniformfactory.test.gradleplugin.simplewrapper.origins;

import com.github.antkudruk.uniformfactory.test.gradleplugin.simplewrapper.FieldValueSource;
import com.github.antkudruk.uniformfactory.test.gradleplugin.simplewrapper.HasWrapper;

@HasWrapper
public class OriginUsingMethod {

    private String name;

    public OriginUsingMethod(String name) {
        this.name = name;
    }

    @FieldValueSource
    public String getName() {
        return name;
    }
}
