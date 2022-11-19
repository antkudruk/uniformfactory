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
                                Adapter.class.getDeclaredMethod("setValue", String.class))
                                .setAnnotation(Marker.class)
                                .parameterSource(String.class, 0)
                                .applyToAny()
                                .addExtends(Integer.class, Integer::parseInt)
                                .finishParameterDescription()
                                .build()
                )
                .build());
    }
}
