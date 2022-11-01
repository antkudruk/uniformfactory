package com.github.antkudruk.uniformfactory.exception;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactoryException;

public class AlienMethodException extends ClassFactoryException {
    public AlienMethodException(String message) {
        super(message, null);
    }
}
