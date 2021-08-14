package com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist;

import com.github.antkudruk.uniformfactory.pluginbuilder.WrapperPlugin;

public class PluginImpl extends WrapperPlugin {
    public PluginImpl( ) {
        super(
                Origin.class,
                "getWrapper",
                Wrapper.class,
                Marker.class,
                "examplePlugin",
                ClassFactoryGeneratorImpl.class);
    }
}