package com.github.antkudruk.uniformfactory.test.gradleplugin.empty;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.pluginbuilder.DefaultMetaClassFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ClassFactoryGeneratorImpl extends DefaultMetaClassFactory<ClassFactoryGeneratorImpl.Wrapper> {

    public interface Wrapper {
    }

    public interface Origin {
        Wrapper getWrapper();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Marker {
    }


    public ClassFactoryGeneratorImpl() {
        super(new ClassFactory.Builder<>(Wrapper.class)
                .build());
    }
}