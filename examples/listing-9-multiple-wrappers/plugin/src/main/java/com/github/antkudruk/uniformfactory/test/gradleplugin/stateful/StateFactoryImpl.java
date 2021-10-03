package com.github.antkudruk.uniformfactory.test.gradleplugin.stateful;

import com.github.antkudruk.uniformfactory.pluginbuilder.MetaClassFactory;

import java.util.function.Function;

public class StateFactoryImpl implements MetaClassFactory<State> {
    @Override
    public <O> Function<O, ? extends State> generateMetaClass(Class<O> originClass) {
        return e -> new State((Origin)e);
    }
}
