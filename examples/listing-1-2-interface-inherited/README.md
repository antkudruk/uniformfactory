# Using Explicit Interface as an Adapter

Here is an example using an explicit interface to generate wrappers by the 
[Uniform Factory](https://github.com/antkudruk/uniformfactory). 

You can avoid class cast made in the previous example the following way:

* Add `@Marker` annotation to the `Origin` interface.

* Add a default implementation to the `Origin` interface to prevent compilation
error. This default implementation should basically throw an exception, but you
may implement the different logic.

```
@Marker
public interface Origin {
    default Wrapper getWrapper() {
        throw new RuntimeException("Wrapper method hasn't been implemented.");
    }
}
```

* Add `@Inherited` to the Marker annotation. 

Java doesn't support interface inheritance itself. However **Uniform Factory** 
enables this inheritance. Only for **Marker Interfaces** though. 

Indeed, only existence of the annotation is required to implement wrapper or 
not. So there is no problem of multiple inheritance here.

```
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Inherited
public @interface Marker { }
```

* Add `Origin` interface to the interfaces list of each origin class.

```
@Marker
public class OriginImpl implements Origin {
}
```
After that, the `OriginImpl` class is going to have an implementation for 
`getWrapper()` method that returns the appropriate wrapper. 

```
OriginImpl origin = new OriginImpl();
Wrapper wrapper = origin.getWrapper();
```

This way, if you implement `getWrapper()` method explicitly, your 
implementation it's not going to be overridden by the plugin. 
