package com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.tree.domain;

import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.tree.Label;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.tree.TreeElementMarker;

@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
@TreeElementMarker
public class Employee {
    @Label
    private final String name;

    public Employee(String name) {
        this.name = name;
    }
}
