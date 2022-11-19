package com.github.antkudruk.uniformfactory.test.gradleplugin.setter;

import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.AnyParameterFilter;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialMapperImpl;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.ParameterValue;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.pluginbuilder.DefaultMetaClassFactory;
import net.bytebuddy.description.type.TypeDescription;

public class ClassFactoryGeneratorImpl extends DefaultMetaClassFactory<Adapter> {
    public ClassFactoryGeneratorImpl() throws ReflectiveOperationException {
        super(new ClassFactory.Builder<>(Adapter.class)
                .addMethodMap(Adapter.class.getDeclaredMethod("setters"), Fun.class)
                .annotationMapElementSource()
                .setMarkerAnnotation(Css.class, Css::value)

                .setterElementFactory()
                .parameterSource(String.class, 0)
                .applyTo(new AnyParameterFilter())
                .addExtends(long.class, Long::parseLong)
                .addExtends(Integer.class, Integer::parseInt)
                .finishParameterDescription()
                .finishElementFactory()
                .endElementSource()
                .endMethodDescription()
                .build()
        );
    }
}
