package com.github.antkudruk.uniformfactory.container;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class WrapperMetaFactoryImpl implements WrapperMetaFactory {
    public static final WrapperMetaFactoryImpl INSTANCE = new WrapperMetaFactoryImpl();
    @Override
    public <W> WrapperFactory<W> get(ClassFactory<W> classFactory) {
        Map<Class<?>, Function<?, W>> classContainers = new HashMap<>();
        return new WrapperFactoryImpl<>(classFactory, classContainers);
    }
}
