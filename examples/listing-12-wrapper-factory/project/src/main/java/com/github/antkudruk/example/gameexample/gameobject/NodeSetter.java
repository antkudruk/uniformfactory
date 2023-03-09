package com.github.antkudruk.example.gameexample.gameobject;

import com.github.antkudruk.example.gameexample.gameengine.Node;

public interface NodeSetter {
    void setNode(Node node);
    Class<?> nodeType();
}
