package com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.pluginbuilder.DefaultMetaClassFactory;

public class ClassFactoryGeneratorImpl extends DefaultMetaClassFactory<Wrapper> {
    public ClassFactoryGeneratorImpl() throws NoSuchMethodException {
        super(new ClassFactory.ShortcutBuilder<>(Wrapper.class)
                .addMethodSingleton(Identity.class, Wrapper.class.getMethod("getIdentity"), String.class)
                .addResultTranslator(Long.class, Object::toString)
                .endMethodDescription()
                .build());
    }
}
