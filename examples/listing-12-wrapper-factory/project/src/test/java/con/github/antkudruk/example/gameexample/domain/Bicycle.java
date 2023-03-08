package con.github.antkudruk.example.gameexample.domain;

import con.github.antkudruk.example.gameexample.gameobject.JmeObject;
import con.github.antkudruk.example.gameexample.gameengine.Node;

public class Bicycle {

    @JmeObject(nodeName = "frontWheel")
    private Node frontWheel;

    @JmeObject(nodeName = "backWheel")
    private Node backWheel;
}
