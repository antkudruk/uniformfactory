package con.github.antkudruk.example.gameexample.gameobject;

public interface PropertyAdapter {
    Class<?> getPropertyType();
    void set(Object newValue);
}
