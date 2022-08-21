package com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.tree;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.pluginbuilder.DefaultMetaClassFactory;

import java.util.ArrayList;
import java.util.List;

public class ClassFactoryGeneratorImpl extends DefaultMetaClassFactory<TreeElement> {
    public ClassFactoryGeneratorImpl() throws ReflectiveOperationException {
        super(new ClassFactory.Builder<>(TreeElement.class)

                .addMethodSingleton(TreeElement.class.getMethod("getLabel"), String.class)
                .setMarkerAnnotation(Label.class)
                .endMethodDescription()

                .addMethodSingleton(TreeElement.class.getMethod("nested"), List.class)
                .setMarkerAnnotation(Nested.class)
                .setDefaultValue(new ArrayList<>())
                .endMethodDescription()

                .build());
    }
}
