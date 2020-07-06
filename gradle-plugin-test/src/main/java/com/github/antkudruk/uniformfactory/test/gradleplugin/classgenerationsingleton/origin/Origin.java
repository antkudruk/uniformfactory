package com.github.antkudruk.uniformfactory.test.gradleplugin.classgenerationsingleton.origin;

import com.github.antkudruk.uniformfactory.test.gradleplugin.classgenerationsingleton.wrapper.Wrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface Origin {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Marker {

    }

    Wrapper getWrapper();
}
