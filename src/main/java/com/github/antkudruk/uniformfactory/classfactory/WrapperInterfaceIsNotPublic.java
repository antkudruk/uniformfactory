package com.github.antkudruk.uniformfactory.classfactory;

public class WrapperInterfaceIsNotPublic extends ClassFactoryException {
    public WrapperInterfaceIsNotPublic(Class<?> wrapperClass) {
        super(String.format("Wrapper class %s must be public.", wrapperClass.getName()),
                null);
    }
}
