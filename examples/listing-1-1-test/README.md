# Empty Adapter Example

## Example Structure

There example contains two projects:
 
  * `plugin` stands for the example of framework
  * `client` application that uses (in our cse just tests) the framework
 
The project `plugen` is not aware about the project `client`. Thus, you can
apply `plugin` to any other application.

### Plugin Project 

Here is an example of **Wrapper** interface to be implemented by wrapper 
classes. It doesn't have any method so far. It's shown here just for the sake 
of demonstrating the 
[Uniform Factory](https://github.com/antkudruk/uniformfactory) usecase. 

```
public interface Wrapper { }
```

We can also write a wrapper that provides the `Origin` object. `ClassFactory` 
creates read-only property `origin` under the hood. So is we describe a method
`getOrigin` in the Wrapper interface, it's going to provide the Origin object 
automatically.

```
public interface Wrapper {
    Origin getOrigin();
}
```

We are going to tell **Uniform Factory** to force each **origin** class 
implement `Origin` interface behind the scenes to access the wrapper object. 
Any `Origin` interface should contain a method returning `Wrapper` instance for
the object.

```
// An interface to implement by any Origin class behind the scenes
public interface Origin {
    Wrapper getWrapper();
}
```

And we will use the following annotation to let Gradle Plugin know which 
classes have to be wrapped.

```
// Marks classes enhanced by Wrapper
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Marker { }
```

Keep in mind that the `@Marker` annotation must have `@Retention` level at 
least `RetentionPolicy.CLASS`.

We'll use *Byte Buddy gradle plugin* to transform origin classes. The procedure 
of using that plugin is the following.

 * Define your plugin class
 * Specify your plugin class in `byteBuddy.transformation` parameter
 * Apply *Byte buddy gradle plugin*
 
Let's extend our plugin class from the default implementation `WrapperPlugin` 
implementation.   

```
public class PluginImpl extends WrapperPlugin<Wrapper> {
    public PluginImpl() {
        super(
                Origin.class,
                Wrapper.class,
                Marker.class,
                "examplePlugin",
                ClassFactoryGeneratorImpl.class);
    }
}
```

Let's consider constructor parameters:

 * `Origin.class` and `Wrapper.class` are **origin interface** and **wrapper 
   interface** correspondingly.
   
 * Default plugin implementation transforms any class annotated with 
   `Marker.class` annotation, that's why the third parameter is needed.
   
 * `"examplePlugin"` is the name of the plugin. It should match Java 
   identifier name and should be unique among the plugins used by the common 
   application.
   
 * `ClassFactoryGeneratorImpl` is a meta class generator. It is a singleton per 
   application. It implements `MetaClassFactory` interface that will be 
   discovered later. `MetaClassFactory` instance has to have a default 
   constructor to be instantiated by `WrapperPlugin` object.  
 
```
public class ClassFactoryGeneratorImpl extends DefaultMetaClassFactory<Wrapper> {
    public ClassFactoryGeneratorImpl() {
        super(new ClassFactory.Builder<>(Wrapper.class)
                .build());
    }
}
```

### Client Project

In your `build.gradle` script, you should refer the plugin to apply it.
The plugin class is set up in byteBuddy.transformation` property:
  
```
build {
    byteBuddy {
        transformation {
            plugin = com.github.antkudruk.uniformfactory.test.gradleplugin.emptywrapper.PluginImpl
        }
    }
}
```

After applying, the plugin will transform each class marked with `@Marker`. 
It's doing to annotation implement `Wrapper` interface on the class loading. 
For instance, when you declare the following class:

```
@Marker
public class OriginImpl { }
```

You'll be able to get the objects wrapper:

```
Object origin = new OriginImpl();
Wrapper wrapper = ((Origin)origin).getWrapper();
```

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
