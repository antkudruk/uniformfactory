package com.github.antkudruk.uniformfactory.pluginbuilder.exceptions;

public class AdaptorFieldNotSpecifiedException extends PluginBuilderException {
    public AdaptorFieldNotSpecifiedException() {
        super("No adaptor factory field name specified");
    }
}
