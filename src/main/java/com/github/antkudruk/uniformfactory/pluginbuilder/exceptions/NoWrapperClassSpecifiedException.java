package com.github.antkudruk.uniformfactory.pluginbuilder.exceptions;

public class NoWrapperClassSpecifiedException extends PluginBuilderException {
    public NoWrapperClassSpecifiedException(String wrapperField) {
        super(String.format("No wrapper class specified for wrapper '%s'",
                wrapperField));
    }
}
