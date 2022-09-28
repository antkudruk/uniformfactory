package com.github.antkudruk.uniformfactory.pluginbuilder.exceptions;

public class NoClassFactoryException extends PluginBuilderException {
    public NoClassFactoryException(String wrapperField) {
        super(String.format("No class factory specified for wrapper '%s'",
                wrapperField));
    }
}
