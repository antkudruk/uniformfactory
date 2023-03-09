package com.github.antkudruk.example.gameexample.domain;

import com.github.antkudruk.example.gameexample.gameobject.JmeObject;
import com.github.antkudruk.example.gameexample.gameengine.Node;

public class Bicycle {

    @JmeObject(nodeName = "frontWheel")
    private Node frontWheel;

    @JmeObject(nodeName = "backWheel")
    private Node backWheel;
}
