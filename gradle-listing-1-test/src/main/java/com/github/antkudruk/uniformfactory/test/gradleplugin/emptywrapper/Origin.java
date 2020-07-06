package com.github.antkudruk.uniformfactory.test.gradleplugin.emptywrapper;

// An interface to implement by any OriginImpl class behind the scenes
public interface Origin {
    Wrapper getWrapper();
}
