package com.github.antkudruk.uniformfactory.test.gradleplugin.methodmap;

import com.github.antkudruk.uniformfactory.pluginbuilder.WrapperPlugin;

public class PluginImpl extends WrapperPlugin<PointWrapper> {
    public PluginImpl( ) {
        super(
                Point.class,
                PointWrapper.class,
                Marker.class,
                "examplePlugin",
                ClassFactoryGeneratorImpl.class);
    }
}