package com.github.antkudruk.uniformfactory.pluginbuilder.exceptions;

public class WrapperFieldNotSpecifiedException extends PluginBuilderException {
    public WrapperFieldNotSpecifiedException() {
        super("No adaptor factory field name specified");
    }
}
