package com.github.antkudruk.uniformfactory.test.gradleplugin.typemapper;

import com.github.antkudruk.uniformfactory.singleton.descriptors.ResultMapperCollection;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.pluginbuilder.DefaultMetaClassFactory;
import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.AnyParameterFilter;
import com.github.antkudruk.uniformfactory.singleton.argument.typemapper.ParameterMappersCollection;
import net.bytebuddy.description.type.TypeDescription;

public class ClassFactoryGeneratorImpl extends DefaultMetaClassFactory<Wrapper> {

    private static ParameterMappersCollection<Integer> parameterMapper = new ParameterMappersCollection<>(Integer.class)
            .add(new TypeDescription.ForLoadedType(String.class), Object::toString)
            .add(new TypeDescription.ForLoadedType(Long.class), Integer::longValue);

    private static ResultMapperCollection<String> resultMapperCollection = new ResultMapperCollection<>(String.class)
            .addMapper(Long.class, Object::toString)
            .addMapper(int.class, Object::toString);

    public ClassFactoryGeneratorImpl() throws NoSuchMethodException {
        super(new ClassFactory.ShortcutBuilder<>(Wrapper.class)

                .addMethodSingleton(FirstMethodMarker.class, Wrapper.class.getMethod("processFirst", Integer.class), String.class)
                .setResultMapper(resultMapperCollection)
                .parameterSource(Integer.class, 0)
                .applyTo(new AnyParameterFilter())
                .setMapper(parameterMapper)
                .finishParameterDescription()
                .endMethodDescription()

                .addMethodSingleton(SecondMethodMarker.class, Wrapper.class.getMethod("processSecond", Integer.class), String.class)
                .setResultMapper(resultMapperCollection)
                .parameterSource(Integer.class, 0)
                .applyTo(new AnyParameterFilter())
                .setMapper(parameterMapper)
                .finishParameterDescription()
                .endMethodDescription()

                .build());
    }
}
