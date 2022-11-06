# Library for Generating Adapters Based On Annotations in Java
**UniformFactory** is a
[Java library to generate adapters (wrappers) based on reflection](https://github.com/antkudruk/uniformfactory).
Adapters are classes implementing a common interface to manipulate different
classes the common way.

In the **Uniform Factory** library, we're using the term **wrapper**
to denote adapter class.

## Why do you Need to Generate Adapters in Java?

Supposing you define some annotations to mark arbitrary class members with.
The classes may have different structure.
You even may be not aware of the classes structure if you publish your
functionality as a framework to share it with other developers. The task is
just to process the annotated class members.

The common way of doing that is to look through annotated class members each
time you use them, find them and process. However, that approach is too
cumbersome. It's slow as well because reflective operations are too expensive.

You'd like to process the annotated members more convenient way.

**Uniform Factory** allows you just to define a common interface for the classes.
We'll call the interface **wrapper interface**. Then you let
**Uniform Factory** automatically generate an implementation of that interface
for each **origin** class. Then you can access annotated members using the
common wrapper interface regardless of a class structure.

**Uniform Factory** works at the bytecode level. Thus, it's significantly faster
than iteration over class members to look for the annotated members.

Let's consider the following example.

You defined `@Label` annotation to mark a member to identify an
object. And you defined `@Propery` annotation to mark an objects named
properties. You don't know the **origin** classes structure. One of the
**origin** classes may look like in the following listing:

```
@Marker
class OriginImpl {
    @Label
    private String name;
    
    @Property('width')
    private long width;
    
    @Property('height')
    public long getHeight() {
        // ...
    }
}
```      

First, you should define the following interfaces to generate wrapper
implementations from:

* **Wrapper** interface. It's a common interface to generate the wrapper
  classes from. A wrapper class is generated for each Origin class.
* **Origin** interface to access Wrapper interface. It contains a single
  method returning the wrapper object.
* **Functional** interfaces. As soon as in this example you define an
  annotation to apply to multiple class members annotated with the same
  annotation, you have to define a functional interface to access each
  member.

```
// Your wrapper interface
interface Wrapper {
    String getName();
    Map<String, Property> getProperties();
}

// Your origin interface
interface Origin {
    Wrapper getWrapper();
}

// Your functional interface.
interface Property {
    Object get();
}
```

Then you should preform some settings up to let **Uniform Factory** know how
wrappers should operate origin class members. See the further chapters to get
an insight on the setting up procedure.

After applying **Uniform Factory**, you can operate the annotated members the
following way.

```
Wrapper wrapper = ((Origin)origin).getWrapper();
wrapper.getName();
Property widthProperty = wrapper.getProperties().get("width");
widthProperty.get();
```

The plugin does the following:

* Makes the class implement `Origin` interface.
* Generates implementation of `Wrapper` and `Property` interface **for each
  origin class**.
* Implements `getWrapper()` method to get the `Wrapper` implementation

For instance, this class

```
@Marker
class Origin {
    //
    // Annotated class members are defined here
    //
}
```

becomes transformed to this one

```
@Marker
class Origin implements OriginInterface {
    //
    // Annotated class members are defined here
    // 
    
    private WrapperInterface wrapper = new Wrapper();
    
    @Override
    public WrapperInterface getWrapper() {
        return wrapper;
    }
}
```

The following tutorial describes how to set up **Uniform Factory** to generate
wrapper classes properly.

## Installing Uniform Factory Into Your Project

You can download **Uniform Factory** into your project from Maven Central.

Here is an example for Gradle:

```
dependencies {
   compile group: 'com.github.antkudruk', name: 'uniform-factory', version: '0.6.0'
}
```

and for Maven:

```
<dependency>
    <groupId>com.github.antkudruk</groupId>
    <artifactId>uniform-factory</artifactId>
    <version>0.6.0</version>
</dependency>
```

**Uniform Factory** is written to be applied in ByteBuddy Gradle Plugin to
generate uniformed **wrapper** classes. Just import and apply  
`byte-buddy-gradle-plugin` and specify your plugin class.

Here is an example for Gradle:

```
plugins {
    id 'java'
    id "net.bytebuddy.byte-buddy-gradle-plugin" version "1.12.6"
}

byteBuddy {
    transformation {
        plugin = // Specify reference to your plugin class here, see the next chapter
    }
}
```

and in Maven:

```
    <plugin>
        <groupId>net.bytebuddy</groupId>
        <artifactId>byte-buddy-maven-plugin</artifactId>
        <version>1.12.6</version>
        <executions>
            <execution>
                <goals>
                    <goal>transform</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <transformations>
                <transformation>
                    <plugin><!-- Specify your plugin class reference here --></plugin>
                </transformation>
            </transformations>
        </configuration>
    </plugin>
```

Let's take a look at some examples.

## Examples

You can find compilable 
[example](https://github.com/antkudruk/uniformfactory/tree/develop/examples)
folder of this project.

### Empty Adapter

Here is an example of an empty wrapper. Empty wrapper is an interface that
doen't define any methods. Even though an empty wrapper is practically useless,
it's a good point to start.

This plugin example adds an empty wrapper to an object satisfying a
special criteria. Furthermore, the plugin makes these object implement
the `Origin` interface. Thus, you can access the wrapper the following way:

```
Object origin = new OriginImpl();
Wrapper wrapper = ((Origin)origin).getWrapper();
```

You can find th whole compilable example that implements an empty wrapper
[here](https://github.com/antkudruk/uniformfactory/tree/develop/examples/listing-1-1-test)

### Using Explicit Interface as an Adapter

Yon can avoid class cast from the previous example. To do it, your domain class
should implement the `Origin` interface. If you mark `Origin` interface with
the `@Marker` annotation, the standard **Uniform Factory** plugin is going to
add the wrapper into this domain class:

```
@Marker
public interface Origin {
    default Wrapper getWrapper() {
        throw new RuntimeException("Wrapper method hasn't been implemented.");
    }
}
```

You can find an example of an explicit interface
[here](https://github.com/antkudruk/uniformfactory/tree/develop/examples/listing-1-2-interface-inherited)

### Select Type Criteria

You may specify custom criteria to choose classes to add adapters to. For
example, matching class names to a special regular expression.
**UniformFactory** provides a flexible way to select particular classes for
that.

Let's implement a plugin to add wrappers to methods that explicitly implement
the `Origin` interface, but using custom class selection criteria.

```
public class PluginImpl extends WrapperPlugin<Wrapper> {
    public PluginImpl() {
        super(
                Origin.class,
                Wrapper.class,
                // Class selection criteria
                td -> td.getInterfaces()
                        .stream()
                        .map(TypeDefinition::asErasure)
                        .anyMatch(new TypeDescription.ForLoadedType(Origin.class)::equals),
                "examplePlugin",
                ClassFactoryGeneratorImpl.class);
    }
}
```

See compilable code
[here](https://github.com/antkudruk/uniformfactory/tree/develop/examples/listing-1-3-custom-select-type-criteria)

### Using Abstract Class as an Adapter

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

Note that the abstract class **must be public**.

You can use that kind of wrapper exactly the same way as for the
interface case. See compilable exampless and detailed description
[here](https://github.com/antkudruk/uniformfactory/tree/develop/examples/listing-8-wrapper-class)

## Generate Adapter that Implements Method Singleton

Let's enhance our empty `Wrapper` class.

Supposing we need to mark different class members to provide an object
identity, just like in the following listing:

```
@Marker
public class Origin1 {
    @Identity
    private Long number = 10L;
}

@Marker
public class Origin2 {
    @Identity
    public String getName() {
        return "name";
    }
}
``` 

We need the common interface to get these identities:

```
    public interface Wrapper {
        String getId();
    }
```

How can we achieve this behaviour with **UniformFactory**?
You can find a compilable example
[here](https://github.com/antkudruk/uniformfactory/tree/develop/examples/listing-3-method-singleton)

### Application: Tree

Let's consider an example.
Supposing we have the following structure of objects:

* Company
* Department
* Employee

Each class has a method returning nested object. And each object has a label
string to render. We'd like to make a uniform tree structure to use by UI to
render.

```
// Wrapper interface
public interface TreeElement {
    String getLabel();
    List<HasTreeElement> nested();
}

// Origin interface
public interface HasTreeElement {
    TreeElement getTreeElement();
}
``` 

You can find an example of a tree
[here](https://github.com/antkudruk/uniformfactory/tree/develop/examples/listing-2-tree-example)

## Generate Wrappers that Provides List Of Methods

We'd like to be able to mark multiple class members with an annotation and
work with them. How can we do that?

We can define the common interface containing the method:

```
public interface Processor {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    @interface Process {

    }

    boolean process(String eventName);
}
```

Then we make our `Wrapper` interface return an element of that functional interface:

```
public interface Wrapper {
    List<Processor> getProcessors();
}
```

You can find a compilable example here:
[here](https://github.com/antkudruk/uniformfactory/tree/develop/examples/listing-4-2-custom-method-list)

## Custom MetaclassGenerator to Generate Wrappers in Java

In the previous examples we used only default implementation
`DefaultMetaClassFactory`, implementing interface `MetaClassFactory`.
`MetaClassFactory` instance is a singleton per application.

Supposing, in the previous task we'd like to store a link to each object

Let's define custom `MetaClassFactory`.

```
public class ClassFactoryGeneratorImpl implements MetaClassFactory<Wrapper> {

    private final ClassFactory<Wrapper> classFactory;

    public ClassFactoryGeneratorImpl() throws NoSuchMethodException {
        this.classFactory = new ClassFactory.Builder<>(Wrapper.class)
                .addMethodList(
                        Wrapper.class.getMethod("getProcessors"),
                        boolean.class
                )
                .setMarkerAnnotation(Processor.Process.class)
                .setFunctionalInterface(Processor.class)

                .addResultTranslator(void.class, t -> true)
                .addResultTranslator(Long.class, t -> t >= 0)
                .addResultTranslator(String.class, "yes"::equalsIgnoreCase)
                .addResultTranslator(Boolean.class, t -> t)

                .parameterSource(String.class, 0)
                .applyTo(new AnyParameterFilter())
                .addTranslator(Integer.class, Integer::parseInt)
                .finishParameterDescription()

                .endMethodDescription()

                .build();
    }

    @Override
    public <O> Function<O, ? extends Wrapper> generateMetaClass(Class<O> originClass) {
        try {
            Constructor<? extends Wrapper> wrapperConstructor = classFactory
                    .build(new TypeDescription.ForLoadedType(originClass))
                    .load(DefaultMetaClassFactory.class.getClassLoader())
                    .getLoaded()
                    .getConstructor(originClass);

            return new WrapperObjectGenerator<>(wrapperConstructor);
        } catch (ClassGeneratorException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static class WrapperObjectGenerator<O> extends DefaultMetaClassFactory.WrapperObjectGenerator<O, Wrapper> {

        WrapperObjectGenerator(Constructor<? extends Wrapper> wrapperConstructor) {
            super(wrapperConstructor);
        }

        @Override
        public Wrapper apply(O t) {
            Wrapper w = super.apply(t);
            CallableObjectsRegistry.INSTANCE.addObject(w);
            return w;
        }
    }
}
```

It's a good practice to move event notification functionality to a separate
class (named `CallableObjectsRegistry` for instance). Let's implement it.

```
public class CallableObjectsRegistry {

    public static final CallableObjectsRegistry INSTANCE = new CallableObjectsRegistry();

    private final WeakHashMap<Wrapper, Object> object = new WeakHashMap<>();

    void addObject(Wrapper wrapper) {
        object.put(wrapper, null);
    }

    public boolean call(String eventName) {
        return object.keySet().stream()
                .map(Wrapper::getProcessors)
                .flatMap(Collection::stream)
                .map(t -> t.process(eventName))
                .reduce(true, (a, b) -> a & b );
    }
}
``` 

In case you're not familiar with Weak References, in a nutshell
`java.util.WeakHashMap` allows to store references as keys and avoid holding
objects in the memory after all the hard references to the objects are removed.

You can call `assertTrue(CallableObjectsRegistry.INSTANCE.call(EVENT_TYPE_STRING));`
to trigger the events and cause event handler methods to be invoked..

After that you can define classes processing

```
@Marker
public class Origin2 {

    private final Function<String, String> consumerString;
    private final Function<Integer, Boolean> consumerInteger;

    public Origin2(Function<String, String> consumerString, Function<Integer, Boolean> consumerInteger) {
        this.consumerString = consumerString;
        this.consumerInteger = consumerInteger;
    }

    @Processor.Process
    public String processString(String event) {
        return consumerString.apply(event);
    }

    @Processor.Process
    public Boolean processInteger(Integer event) {
        return consumerInteger.apply(event);
    }
}
```

## Generate Adapters with Map of Methods

In the previous example, we just took all annotated class members. But what if
we'd like to use some additional information?

We can use a map instead of a list. UniformFactory takes keys from the
annotation parameters and generate values implementing functional interface:

```
public interface Coordinate {
    long getCoordinate(Long scale);
}

public interface PointWrapper {
    Map<String, Coordinate> getCoords();
}
```

You can find a compilable example
[here](https://github.com/antkudruk/uniformfactory/tree/develop/examples/listing-6-method-map)

### Using Multiple Wrappers

**Uniform Factory** can generate multiple wrappers for one object.

Where it may be convenient?

Adapters generated by **UniformFactory** are stateless. But what if you're going to
enhance your objects with state? For instance, with cache.

You can use two wrappers:
* An adapter generated by **Uniform Factory**
* Your cache object that works with the adapter

The example of code for multiple adapters at one origin class may be found
[here](https://github.com/antkudruk/uniformfactory/tree/develop/examples/listing-9-multiple-wrappers)

### Setting up a Field in the Origin Class

You can do setting up the field marked with an annotation exactly the same way
as MethodSingleton does. 
See the example 
[here](https://github.com/antkudruk/uniformfactory/tree/develop/examples/listing-10-setter)

### Setting up Multiple Fields

UniformFactory may implement adapters for multiple fields in the origin class
for you. See the example 
[here](https://github.com/antkudruk/uniformfactory/tree/develop/examples/listing-11-setter-map)

### Translating parameters and result

#### Translating result

Let's take a look at the following example.
We have a wrapper containing two methods. Both methods return the same type
and consume the same types, like in the following example.

```
public interface Wrapper {
    String processFirst(Integer scale);
    String processSecond(Integer scale);
}
```

To follow DRY (Don't Repeat Yourself) principle, it's better to use the common
result and parameter translators to avoid adding them twice. `setResultMapper`
and `setMapper` methods will help you to use specified mappers.

```
public class ClassFactoryGeneratorImpl extends DefaultMetaClassFactory<Wrapper> {

    private static ParameterMappersCollection<Integer> parameterMapper = new ParameterMappersCollection<>(Integer.class)
            .add(new TypeDescription.ForLoadedType(String.class), Object::toString)
            .add(new TypeDescription.ForLoadedType(Long.class), Integer::longValue);

    private static ResultMapperCollection<String> resultMapperCollection = new ResultMapperCollection<>(String.class)
            .addMapper(Long.class, Object::toString)
            .addMapper(int.class, Object::toString);

    public ClassFactoryGeneratorImpl() throws NoSuchMethodException {
        super(new ClassFactory.Builder<>(Wrapper.class)

                .addMethodSingleton(FirstMethodMarker.class, Wrapper.class.getMethod("process", Integer.class), String.class)
                .setResultMapper(resultMapperCollection)
                .parameterSource(Integer.class, 0)
                .applyTo(new AnyParameterFilter())
                .setMapper(parameterMapper)
                .finishParameterDescription()
                .endMethodDescription()

                .addMethodSingleton(SecondMethodMarker.class, Wrapper.class.getMethod("processSecond", Integer.class), String.class)
                .setResultMapper(resultMapperCollection)
                .parameterSource(Integer.class, 0)
                .applyTo(new AnyParameterFilter())
                .setMapper(parameterMapper)
                .finishParameterDescription()
                .endMethodDescription()

                .build());
    }
}

```

## History

| version | Description                                                       |
|---------|-------------------------------------------------------------------|
| 0.2.2   | Added pure ByteBuddy implementation                               |
| 0.3.0   | Gave up builder experiments                                       |
| 0.4.0   | Added an opportunity to implement custom method map               |
| 0.4.1   | Clean up exceptions                                               |
| 0.5.1   | Allowed subclasses in element factories. Cleaned up method        |
|         | collections                                                       |
| 0.5.2   | Cleaned up messages in some exceptions.                           |
|         | Cleaned up method descriptors and builders from spare properties. |
| 0.5.3   | Small cleanup                                                     |
| 0.6.0   | Fixed bug for parameter mapper super types.                       |
|         | Added automated boxing of primitives.                             |
|         | Got rid of default translators in children mappers                |

## License

```
Copyright 2020 - Present Anton Kudruk

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
