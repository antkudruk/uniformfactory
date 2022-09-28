package com.github.antkudruk.uniformfactory.pluginbuilder.exceptions;

public class AdaptorFactoryFieldNotSpecifiedException extends PluginBuilderException {
    public AdaptorFactoryFieldNotSpecifiedException(String wrapperField) {
        super(String.format("No mapper factory field name specified for the mapper '%s'",
                wrapperField));
    }
}
