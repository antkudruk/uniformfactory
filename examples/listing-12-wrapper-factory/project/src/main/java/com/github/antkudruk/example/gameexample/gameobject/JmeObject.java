package com.github.antkudruk.example.gameexample.gameobject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface JmeObject {
    String nodeName();
}
