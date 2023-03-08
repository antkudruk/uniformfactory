package con.github.antkudruk.example.gameexample.gameobject;

import con.github.antkudruk.example.gameexample.gameengine.Node;

public interface NodeSetter {
    void setNode(Node node);
    Class<?> nodeType();
}
