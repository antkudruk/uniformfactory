package com.github.antkudruk.uniformfactory.test.gradleplugin.stateful;

@Marker
public interface Origin {
    default Wrapper getAdapter() {
        throw new RuntimeException();
    }

    default State getState() {
        throw new RuntimeException();
    }
}
