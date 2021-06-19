package com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter;

import com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.wrapper.Origin;
import com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.wrapper.Wrapper;
import com.github.antkudruk.uniformfactory.pluginbuilder.DefaultMetaClassFactory;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.pluginbuilder.WrapperPlugin;

public class WrapperMetaClassFactory extends DefaultMetaClassFactory<Wrapper> {

    private static final ClassFactory<Wrapper> classFactory;

    static {
        try {
            classFactory = new ClassFactory.ShortcutBuilder<>(Wrapper.class)
                    .addMethodSingleton(
                            Wrapper.class.getMethod("common", String.class, String.class),
                            String.class)
                    .setMarkerAnnotation(MethodMarker.class)

                    .parameterSource(String.class, 0)
                    .applyToAnnotated(First.class)
                    .finishParameterDescription()

                    .parameterSource(String.class, 1)
                    .applyToAnnotated(Second.class)
                    .addTranslator(Boolean.class, "true"::equalsIgnoreCase)
                    .addTranslator(Long.class, Long::parseLong)
                    .finishParameterDescription()

                    .addResultTranslator(Long.class, t -> t.toString() + " units")
                    .addResultTranslator(Boolean.class, t -> t ? "Yes" : "No")

                    .endMethodDescription()

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
                .setClassFactoryGenerator(WrapperMetaClassFactory.class)
                .build();
    }

    public WrapperMetaClassFactory() {
        super(classFactory);
    }
}
