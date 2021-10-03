package com.github.antkudruk.uniformfactory.test.gradleplugin.wrapperclass;

import com.github.antkudruk.uniformfactory.pluginbuilder.WrapperPlugin;

public class PluginImpl extends WrapperPlugin {
    public PluginImpl( ) {
        super(
                Origin.class,
                Wrapper.class,
                Marker.class,
                "examplePlugin",
                ClassFactoryGeneratorImpl.class);
    }
}