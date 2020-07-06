package com.github.antkudruk.uniformfactory.test.gradleplugin.emptywrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Marks classes enhanced by Wrapper
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Marker {
}
