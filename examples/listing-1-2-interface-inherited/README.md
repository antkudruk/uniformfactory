# Using Explicit Interface as an Adapter

## Example Structure

There example contains two projects:
 
  * `plugin` stands for the example of framework
  * `client` application that uses (in our cse just tests) the framework
 
The project `plugen` is not aware about the project `client`. Thus, you can
apply `plugin` to any other application.

### Plugin Project 

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

### Client Example Implementation

To enhance ByteBuddy with a wrapper, you should add `Origin` interface to the 
interfaces list of each origin class.

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

## How to Build and Test the Example

You should perform the following steps to run the project:

* Install `plugin` into your local Maven repository. It's normally in `.m2` 
directory of your home directory. To do it, go to `plugin` directory and run 
the command:
     
```
gradle install
```
  
* Change directory to `client` project and build it.

```
gradle build
``` 
