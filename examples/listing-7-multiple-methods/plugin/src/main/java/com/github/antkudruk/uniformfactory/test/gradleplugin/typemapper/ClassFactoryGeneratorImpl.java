package com.github.antkudruk.uniformfactory.test.gradleplugin.typemapper;

import com.github.antkudruk.uniformfactory.singleton.descriptors.ResultMapperCollection;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.pluginbuilder.DefaultMetaClassFactory;
import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.AnyParameterFilter;
import com.github.antkudruk.uniformfactory.singleton.argument.typemapper.ParameterMappersCollection;
import net.bytebuddy.description.type.TypeDescription;
import com.github.antkudruk.uniformfactory.singleton.argument.typemapper.ExtendsParameterTranslator;
import com.github.antkudruk.uniformfactory.singleton.argument.typemapper.SuperParameterTranslator;

public class ClassFactoryGeneratorImpl extends DefaultMetaClassFactory<Wrapper> {

    private static ParameterMappersCollection<Integer> parameterMapper = new ParameterMappersCollection<>(Integer.class)
            .add(new SuperParameterTranslator<>(String.class, Object::toString))
            .add(new SuperParameterTranslator<>(Long.class, Integer::longValue));

    private static ResultMapperCollection<String> resultMapperCollection = new ResultMapperCollection<>(String.class)
            .addMapper(Long.class, Object::toString)
            .addMapper(int.class, Object::toString);

    public ClassFactoryGeneratorImpl() throws NoSuchMethodException {
        super(new ClassFactory.Builder<>(Wrapper.class)

                .addMethodSingleton(Wrapper.class.getMethod("processFirst", Integer.class), String.class)
                .setMarkerAnnotation(FirstMethodMarker.class)
                .setResultMapper(resultMapperCollection)
                .parameterSource(Integer.class, 0)
                .applyTo(new AnyParameterFilter())
                .setMapper(parameterMapper)
                .finishParameterDescription()
                .endMethodDescription()

                .addMethodSingleton(Wrapper.class.getMethod("processSecond", Integer.class), String.class)
                .setMarkerAnnotation(SecondMethodMarker.class)
                .setResultMapper(resultMapperCollection)
                .parameterSource(Integer.class, 0)
                .applyTo(new AnyParameterFilter())
                .setMapper(parameterMapper)
                .finishParameterDescription()
                .endMethodDescription()

                .build());
    }
}
