package com.github.antkudruk.uniformfactory.test.gradleplugin.stateful;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.pluginbuilder.DefaultMetaClassFactory;

public class WrapperFactoryImpl extends DefaultMetaClassFactory<Wrapper> {
    public WrapperFactoryImpl() throws ReflectiveOperationException {
        super(new ClassFactory.Builder<>(Wrapper.class)
                .addMethodSingleton(Wrapper.class.getMethod("getValue"), Integer.class)
                .setMarkerAnnotation(Marker.class)
                .endMethodDescription()
                .build());
    }
}
