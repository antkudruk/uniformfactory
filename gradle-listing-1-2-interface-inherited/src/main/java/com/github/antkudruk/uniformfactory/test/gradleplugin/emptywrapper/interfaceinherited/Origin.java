package com.github.antkudruk.uniformfactory.test.gradleplugin.emptywrapper.interfaceinherited;

@Marker
public interface Origin {
    default Wrapper getWrapper() {
        throw new RuntimeException("Wrapper method hasn't been implemented.");
    }
}
