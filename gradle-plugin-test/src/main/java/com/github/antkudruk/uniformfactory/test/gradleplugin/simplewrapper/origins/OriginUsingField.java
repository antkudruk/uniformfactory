package com.github.antkudruk.uniformfactory.test.gradleplugin.simplewrapper.origins;

import com.github.antkudruk.uniformfactory.test.gradleplugin.simplewrapper.FieldValueSource;
import com.github.antkudruk.uniformfactory.test.gradleplugin.simplewrapper.HasWrapper;

@HasWrapper
public class OriginUsingField {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    @FieldValueSource
    private final Integer id;

    public OriginUsingField(Integer id) {
        this.id = id;
    }
}
