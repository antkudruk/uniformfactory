package com.github.antkudruk.uniformfactory.test.gradleplugin.emptywrapper.interfaceinherited;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.pluginbuilder.DefaultMetaClassFactory;

public class ClassFactoryGeneratorImpl extends DefaultMetaClassFactory<Wrapper> {
    public ClassFactoryGeneratorImpl() {
        super(new ClassFactory.Builder<>(Wrapper.class)
                .build());
    }
}
