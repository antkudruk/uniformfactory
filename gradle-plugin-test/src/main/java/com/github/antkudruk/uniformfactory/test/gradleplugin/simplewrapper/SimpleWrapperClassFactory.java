package com.github.antkudruk.uniformfactory.test.gradleplugin.simplewrapper;

import com.github.antkudruk.uniformfactory.singleton.descriptors.MethodSingletonDescriptor;
import com.github.antkudruk.uniformfactory.test.gradleplugin.simplewrapper.wrapper.Origin;
import com.github.antkudruk.uniformfactory.test.gradleplugin.simplewrapper.wrapper.Wrapper;
import com.github.antkudruk.uniformfactory.pluginbuilder.DefaultMetaClassFactory;
import com.github.antkudruk.uniformfactory.pluginbuilder.WrapperPlugin;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;

public class SimpleWrapperClassFactory {

    private static final ClassFactory<Wrapper> classFactory;

    static {
        try {
            classFactory = new ClassFactory.Builder<>(Wrapper.class)
                    .addMethodDescriptor(
                            new MethodSingletonDescriptor.ShortcutBuilder<>(
                                    Wrapper.class.getMethod("getId"), String.class)
                                    .setMarkerAnnotation(FieldValueSource.class)
                                    .addResultTranslator(Integer.class, Object::toString)
                                    .build()
                    )
                    .build();
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static WrapperPlugin wrapperPlugin() {
        return new WrapperPlugin.Builder<>(Wrapper.class)
                .setOriginInterface(Origin.class)
                .setGetWrapperMethodName("getWrapper")
                .setTypeMarker(HasWrapper.class)
                .setClassFactoryGenerator(CtorMeta.class)
                .build();
    }

    public static class CtorMeta extends DefaultMetaClassFactory<Wrapper> {
        public CtorMeta() {
            super(classFactory);
        }
    }
}
