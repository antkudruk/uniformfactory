package com.github.antkudruk.uniformfactory.test.gradleplugin.emptywrapper.interfacebased;

public interface Origin {
    default Wrapper getWrapper() {
        throw new RuntimeException("The method should be implemented by the plugin. Please, specify the plugin un your  build script.");
    }
}
