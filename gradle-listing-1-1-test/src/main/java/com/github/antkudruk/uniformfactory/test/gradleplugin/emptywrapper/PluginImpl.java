package com.github.antkudruk.uniformfactory.test.gradleplugin.emptywrapper;

import com.github.antkudruk.uniformfactory.pluginbuilder.WrapperPlugin;

public class PluginImpl extends WrapperPlugin<Wrapper> {
    public PluginImpl() {
        super(
                Origin.class,
                Wrapper.class,
                Marker.class,
                "examplePlugin",
                ClassFactoryGeneratorImpl.class);
    }
}
