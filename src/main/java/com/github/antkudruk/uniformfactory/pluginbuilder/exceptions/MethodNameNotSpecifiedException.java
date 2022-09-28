package com.github.antkudruk.uniformfactory.pluginbuilder.exceptions;

public class MethodNameNotSpecifiedException extends PluginBuilderException {
    public MethodNameNotSpecifiedException(String wrapperField) {
        super(String.format("Method name to get the mapper '%s' is not specified", wrapperField));
    }
}
