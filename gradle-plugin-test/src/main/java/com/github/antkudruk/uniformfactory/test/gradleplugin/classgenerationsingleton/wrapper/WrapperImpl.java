package com.github.antkudruk.uniformfactory.test.gradleplugin.classgenerationsingleton.wrapper;

public class WrapperImpl implements Wrapper {

    private final int classNumber;
    private final int objectNumber;

    public WrapperImpl(int classNumber, int objectNumber) {
        this.classNumber = classNumber;
        this.objectNumber = objectNumber;
    }

    @Override
    public int getClassNumber() {
        return classNumber;
    }

    @Override
    public int getObjectNumber() {
        return objectNumber;
    }
}
