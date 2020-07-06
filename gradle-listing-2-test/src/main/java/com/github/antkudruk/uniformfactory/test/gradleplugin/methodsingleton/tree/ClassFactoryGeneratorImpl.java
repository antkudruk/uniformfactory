package com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.tree;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.pluginbuilder.DefaultMetaClassFactory;

import java.util.ArrayList;
import java.util.List;

public class ClassFactoryGeneratorImpl extends DefaultMetaClassFactory<TreeElement> {
    public ClassFactoryGeneratorImpl() throws ReflectiveOperationException {
        super(new ClassFactory.ShortcutBuilder<>(TreeElement.class)

                .addMethodSingleton(Label.class, TreeElement.class.getMethod("getLabel"), String.class)
                .endMethodDescription()

                .addMethodSingleton(Nested.class, TreeElement.class.getMethod("nested"), List.class)
                .setDefaultValue(new ArrayList<>())
                .endMethodDescription()

                .build());
    }
}
