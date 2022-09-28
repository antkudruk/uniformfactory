package com.github.antkudruk.uniformfactory.pluginbuilder.impls;

import com.github.antkudruk.uniformfactory.pluginbuilder.MetaClassFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.function.Function;

public class MetaClassFactoryImpl implements MetaClassFactory<Adapter> {
    @Override
    public <O> Function<O, ? extends Adapter> generateMetaClass(Class<O> originClass) {
        throw new NotImplementedException();
    }
}