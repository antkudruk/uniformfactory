package com.github.antkudruk.uniformfactory.container;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public final class WrapperFactoryImpl<W> implements WrapperFactory<W> {

    private final ClassFactory<W> classFactory;
    private final Map<Class<?>, Function<?, W>> classContainers;


    @Override
    public <E> W get(E object) throws ClassGeneratorException {
        Class<?> type = object.getClass();
        Function<E, W> classContainer = ((Function<E, W>) classContainers
                .getOrDefault(type, classFactory.buildWrapperFactory(type)));
        classContainers.put(type, classContainer);
        return classContainer.apply(object);
    }
}
