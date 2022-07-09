package com.github.antkudruk.uniformfactory.test.gradleplugin.setter;

import com.github.antkudruk.uniformfactory.methodcollection.SetterElementFactory;
import com.github.antkudruk.uniformfactory.methodmap.descriptors.MethodMapDescriptor;
import com.github.antkudruk.uniformfactory.singleton.argument.filters.ParameterQueryBuilder;
import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.AnyParameterFilter;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialMapperImpl;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.ParameterValue;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.pluginbuilder.DefaultMetaClassFactory;
import com.github.antkudruk.uniformfactory.setter.descriptors.SetterDescriptor;
import net.bytebuddy.description.type.TypeDescription;

public class ClassFactoryGeneratorImpl extends DefaultMetaClassFactory<Adapter> {

    private static final PartialMapperImpl PARAMETER_VALUE = new PartialMapperImpl(
            new AnyParameterFilter(),
            new ParameterValue<>(String.class, 0)
                    .addTranslator(new TypeDescription.ForLoadedType(int.class), i -> {
                        return Integer.parseInt(i);
                    })
                    .addTranslator(new TypeDescription.ForLoadedType(Integer.class), i -> {
                        return Integer.parseInt(i);
                    })
                    .addTranslator(new TypeDescription.ForLoadedType(long.class), i -> {
                        return Long.parseLong(i);
                    })
                    .addTranslator(new TypeDescription.ForLoadedType(Long.class), i -> {
                        return Long.parseLong(i);
                    })
    );

    public ClassFactoryGeneratorImpl() throws ReflectiveOperationException {
        super(new ClassFactory.Builder<>(Adapter.class)
                .addMethodDescriptor(
                        new MethodMapDescriptor
                                .Builder<>(Fun.class, Adapter.class.getDeclaredMethod("setters"), void.class)
                                .setMarkerAnnotation(Css.class, c -> {
                                    return c.value();
                                })
                                .setElementFactory(new SetterElementFactory<Fun, Void>(
                                        Fun.class,
                                        PARAMETER_VALUE        // TODO: Do only once
                                ))
                                .addParameterTranslator(PARAMETER_VALUE)        // TODO: Do only once
                                .build()
                )

                /*
                .addMethodDescriptor(


                        new SetterDescriptor.ShortcutBuilder<>(
                                Adapter.class.getDeclaredMethod("setters", String.class), String.class)
                                .setAnnotation(Marker.class)
                                .addParameterTranslator(
                                        new PartialMapperImpl(
                                                new AnyParameterFilter(),
                                                new ParameterValue<>(String.class, 0)
                                                        .addTranslator(new TypeDescription.ForLoadedType(int.class), Integer::parseInt)
                                                        .addTranslator(new TypeDescription.ForLoadedType(Integer.class), Integer::parseInt)
                                                        .addTranslator(new TypeDescription.ForLoadedType(long.class), Long::parseLong)
                                                        .addTranslator(new TypeDescription.ForLoadedType(Long.class), Long::parseLong)
                                        )
                                )
                                .build()
                )
                */
                .build());
    }
}
