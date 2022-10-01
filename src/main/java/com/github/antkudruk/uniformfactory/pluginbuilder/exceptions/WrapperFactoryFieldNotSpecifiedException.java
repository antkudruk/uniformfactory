package com.github.antkudruk.uniformfactory.pluginbuilder.exceptions;

public class WrapperFactoryFieldNotSpecifiedException extends PluginBuilderException {
    public WrapperFactoryFieldNotSpecifiedException(String wrapperField) {
        super(String.format("No mapper factory field name specified for the mapper '%s'",
                wrapperField));
    }
}
