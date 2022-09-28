package com.github.antkudruk.uniformfactory.pluginbuilder.exceptions;

public class WrongMethodNameException extends PluginBuilderException {
    public WrongMethodNameException(String methodName, Class<?> originClass) {
        super(String.format("No method [%s] defined in the origin class [%s]",
                methodName, originClass.getName()));
    }
}
