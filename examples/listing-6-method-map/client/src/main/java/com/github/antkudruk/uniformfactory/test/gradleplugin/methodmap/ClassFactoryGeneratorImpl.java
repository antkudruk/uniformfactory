package com.github.antkudruk.uniformfactory.test.gradleplugin.methodmap;

import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.AnyParameterFilter;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.pluginbuilder.DefaultMetaClassFactory;

public class ClassFactoryGeneratorImpl extends DefaultMetaClassFactory<PointWrapper> {
    public ClassFactoryGeneratorImpl() throws NoSuchMethodException {
        super(new ClassFactory.ShortcutBuilder<>(PointWrapper.class)

                .addMethodMap(PointWrapper.class.getMethod("getCoords"), Coordinate.class, long.class)
                .setMarkerAnnotation(CoordinateMarker.class, CoordinateMarker::value)
                .parameterSource(Long.class, 0)

                .applyTo(new AnyParameterFilter())
                .addTranslator(boolean.class, t -> t > 0)
                .addTranslator(String.class, Object::toString)
                .addTranslator(long.class, t -> t)

                .finishParameterDescription()
                .addResultTranslator(Boolean.class, t -> t ? 1L : -1L)
                .addResultTranslator(String.class, Long::parseLong)
                .addResultTranslator(int.class, Integer::longValue)

                .endMethodDescription()

                .build());
    }
}
