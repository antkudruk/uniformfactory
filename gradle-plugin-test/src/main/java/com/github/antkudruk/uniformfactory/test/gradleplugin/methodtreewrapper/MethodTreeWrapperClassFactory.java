package com.github.antkudruk.uniformfactory.test.gradleplugin.methodtreewrapper;

import com.github.antkudruk.uniformfactory.test.gradleplugin.methodtreewrapper.wrapper.Fun;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodtreewrapper.wrapper.Origin;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodtreewrapper.wrapper.Wrapper;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.pluginbuilder.DefaultMetaClassFactory;
import com.github.antkudruk.uniformfactory.pluginbuilder.WrapperPlugin;

public class MethodTreeWrapperClassFactory {

    private static final ClassFactory<Wrapper> classFactory;

    static  {
        try {
            classFactory = new ClassFactory.ShortcutBuilder<>(Wrapper.class)

                    .addMethodMap(Wrapper.class.getMethod("getWrappers"), String.class)
                    .setMarkerAnnotation(FunctionalElement.class, FunctionalElement::value)
                    .setFunctionalInterface(Fun.class)

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
                .setClassFactoryGenerator(CtorMeta.class)
                .build();
    }

    public static class CtorMeta extends DefaultMetaClassFactory<Wrapper> {
        public CtorMeta() {
            super(classFactory);
        }
    }
}
