package com.github.antkudruk.uniformfactory.test.gradleplugin.setter;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.methodcollection.SetterElementFactory;
import com.github.antkudruk.uniformfactory.methodmap.descriptors.MethodMapDescriptor;
import com.github.antkudruk.uniformfactory.pluginbuilder.DefaultMetaClassFactory;
import com.github.antkudruk.uniformfactory.setter.descriptors.SetterDescriptor;
import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.ParameterTypeFilter;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialMapperImpl;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialParameterUnion;
import com.github.antkudruk.uniformfactory.singleton.argument.typemapper.ParameterMappersCollection;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.ParameterValue;
import com.github.antkudruk.uniformfactory.singleton.descriptors.ResultMapperCollection;
import net.bytebuddy.description.type.TypeDescription;

import java.util.ArrayList;

public class ClassFactoryGeneratorImpl extends DefaultMetaClassFactory<Adapter> {

    public ClassFactoryGeneratorImpl() throws ReflectiveOperationException {
        super(new ClassFactory.Builder<>(Adapter.class)
                        .addMethodDescriptor(new MethodMapDescriptor.Builder<>(
                                SetterAdapter.class,
                                Adapter.class.getDeclaredMethod("getSetters"),
                                String.class)
                                .setMarkerAnnotation(Marker.class)
                                /*
                                .setElementFactory(
                                        new SetterElementFactory<SetterAdapter, Void>(
                                                SetterAdapter.class,
                                                new ResultMapperCollection(String.class),
                                                new PartialParameterUnion(new ArrayList<>())
                                        )
                                )*/
                        .build()
                );
    }
}
