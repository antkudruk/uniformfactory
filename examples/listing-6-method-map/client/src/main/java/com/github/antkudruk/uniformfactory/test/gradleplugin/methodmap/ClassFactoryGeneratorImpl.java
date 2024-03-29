package com.github.antkudruk.uniformfactory.test.gradleplugin.methodmap;

import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.AnyParameterFilter;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.pluginbuilder.DefaultMetaClassFactory;

public class ClassFactoryGeneratorImpl extends DefaultMetaClassFactory<PointWrapper> {
    public ClassFactoryGeneratorImpl() throws NoSuchMethodException {
        super(new ClassFactory.Builder<>(PointWrapper.class)

                .addMethodMap(PointWrapper.class.getMethod("getCoords"), Coordinate.class)
                .annotationMapElementSource()
                .setMarkerAnnotation(CoordinateMarker.class, CoordinateMarker::value)

                // Creating map of getters
                .getterElementFactory(long.class)

                // Describes mapping of parameter from the adapter type to origin types
                .parameterSource(Long.class, 0)

                .applyTo(new AnyParameterFilter())
                .addExtends(boolean.class, t -> t > 0)
                .addExtends(String.class, Object::toString)
                .addExtends(long.class, t -> t)

                .finishParameterDescription()
                .addResultTranslator(Boolean.class, t -> t ? 1L : -1L)
                .addResultTranslator(String.class, Long::parseLong)
                .addResultTranslator(int.class, Integer::longValue)

                .finishElementFactory()
                .endElementSource()
                .endMethodDescription()

                .build());
    }
}
