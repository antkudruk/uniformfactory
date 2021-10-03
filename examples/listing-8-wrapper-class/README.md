Interfaces are stateless. So you can't have any variables inside interfaces.

But what if you'd like to store a state in your wrapper? For instance, 
you have to make a cache in your wrapper.

You can use a **wrapper** class instead of an interface.

Let's consider an example. Your wrapper hs an accumulator. And the 
accumulator increases by a number from an underlying object. It happens
each time you get the accumulator value.

```
public abstract class Wrapper {

    private int accumulator;

    public int getAccumulated() {
        return accumulator += getDelta();
    }

    public abstract int getDelta();
}

``` 

You can use that kind of wrapper exactly the same way as for the 
interface case. 

* in Class Factory Generator:

```
public class ClassFactoryGeneratorImpl extends DefaultMetaClassFactory<Wrapper> {

    public ClassFactoryGeneratorImpl() throws NoSuchMethodException {
        super(new ClassFactory.ShortcutBuilder<>(Wrapper.class)
                .addMethodSingleton(Wrapper.class.getMethod("getDelta"), int.class)
                .setMarkerAnnotation(Marker.class)
                .endMethodDescription()
                .build());
    }
}
``` 

* And in the plugin:

```
public class PluginImpl extends WrapperPlugin {
    public PluginImpl( ) {
        super(
                Origin.class,
                Wrapper.class,
                Marker.class,
                "examplePlugin",
                ClassFactoryGeneratorImpl.class);
    }
}
``` 
