package com.github.antkudruk.uniformfactory.test.gradleplugin.setter;

import com.github.antkudruk.uniformfactory.test.gradleplugin.setter.Adapter;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.pluginbuilder.DefaultMetaClassFactory;
import com.github.antkudruk.uniformfactory.setter.descriptors.SetterDescriptor;
import com.github.antkudruk.uniformfactory.singleton.argument.typemapper.ParameterMappersCollection;
import net.bytebuddy.description.type.TypeDescription;

public class ClassFactoryGeneratorImpl extends DefaultMetaClassFactory<Adapter> {

    public ClassFactoryGeneratorImpl() throws ReflectiveOperationException {
        super(new ClassFactory.Builder<>(Adapter.class)
                .addMethodDescriptor(
                        new SetterDescriptor.Builder<>(
                                Adapter.class.getDeclaredMethod("setValue", String.class), String.class)
                                .setAnnotation(Marker.class)
                                .setParameterMapper(
                                        new ParameterMappersCollection<>(String.class)
                                                .add(
                                                        new TypeDescription.ForLoadedType(Integer.class),
                                                        Integer::parseInt)
                                                .add(
                                                        new TypeDescription.ForLoadedType(int.class),
                                                        Integer::parseInt)
                                )
                                .build()
                )
                .build());
    }
}
