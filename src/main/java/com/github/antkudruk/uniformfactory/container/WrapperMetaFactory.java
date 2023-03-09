package com.github.antkudruk.uniformfactory.container;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;

public interface WrapperMetaFactory {
    <W> WrapperFactory<W> get(ClassFactory<W> classFactory);
}
