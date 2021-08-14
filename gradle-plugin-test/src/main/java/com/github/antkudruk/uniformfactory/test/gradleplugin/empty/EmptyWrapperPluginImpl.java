package com.github.antkudruk.uniformfactory.test.gradleplugin.empty;

import com.github.antkudruk.uniformfactory.pluginbuilder.WrapperPlugin;

public class EmptyWrapperPluginImpl extends WrapperPlugin {


    public EmptyWrapperPluginImpl( ) {
        super(
                ClassFactoryGeneratorImpl.Origin.class,
                "getWrapper",
                ClassFactoryGeneratorImpl.Wrapper.class,
                ClassFactoryGeneratorImpl.Marker.class,
                "wrapper",
                "wrapperClassFactory",
                ClassFactoryGeneratorImpl.class);
    }
}
