package com.github.antkudruk.uniformfactory.test.gradleplugin.emptywrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO: Remove the comment. Just checking gitlab restrictions
// Marks classes enhanced by Wrapper
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Marker {
}
