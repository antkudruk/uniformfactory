package com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.tree;

import com.github.antkudruk.uniformfactory.pluginbuilder.WrapperPlugin;

public class PluginImpl extends WrapperPlugin {
    public PluginImpl( ) {
        super(
                HasTreeElement.class,
                TreeElement.class,
                TreeElementMarker.class,
                "treeWrapperField",
                "treeClassFactoryField",
                ClassFactoryGeneratorImpl.class);
    }
}
