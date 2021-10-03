package com.github.antkudruk.uniformfactory.test.gradleplugin.wrapperclass;

public abstract class Wrapper {

    private int accumulator;

    public int getAccumulated() {
        return accumulator += getDelta();
    }

    public abstract int getDelta();
}
