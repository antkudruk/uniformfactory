package com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.tree;

import java.util.List;

public interface TreeElement {
    String getLabel();
    List<HasTreeElement> nested();
}
