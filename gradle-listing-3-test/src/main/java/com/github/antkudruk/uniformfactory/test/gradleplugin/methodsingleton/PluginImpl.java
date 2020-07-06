package com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton;

import com.github.antkudruk.uniformfactory.pluginbuilder.WrapperPlugin;

public class PluginImpl extends WrapperPlugin<Wrapper> {
    public PluginImpl( ) {
        super(
                Origin.class,
                "getWrapper",
                Wrapper.class,
                Marker.class,
                "wrapper",
                "wrapperClassFactory",
                ClassFactoryGeneratorImpl.class);
    }
}