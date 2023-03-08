package con.github.antkudruk.example.gameexample.gameobject;

import java.util.Map;

public interface GameObjectAdapter {
    String identity();
    Map<String, NodeSetter> nodeSetters();
    Map<String, PropertyAdapter> nodeProperties();
    Object getOrigin();

    default Class<?> gameObjectType() {
        return getOrigin().getClass();
    }
}
