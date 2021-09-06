# Uniform Factory

## Description

The project is designed to simplify processing annotated class members.

## Tutorial

### Introduction

Supposing you define some annotations to mark arbitrary class members with for 
further processing of that classes. The classes may have different structure or
you even may be not aware of the classes structure if you publish your 
functionality as a framework to share it with another developers. The task is 
just to process the annotated class members. 

The common way of doing that is to look through annotated class members each 
time you use them, find them and process. However that approach is too 
cumbersome. It's slow as well because reflective operations are too expensive. 

You'd like to process the annotated members more convenient way, aren't you? 

With **Uniform Factory** you can just define a common interface to get 
annotated class members (we'll call it **wrapper interface**) and let 
**Uniform Factory** automatically generate an implementation of that interface 
for each **origin** class. Then you can access annotated members using the 
common wrapper interface regardless of a class structure. 

It works at the bytecode level so it's significantly faster than iteration over
class members to look for the annotated members. 

Let's consider the following example.
You defined `@Label` annotation to mark a member to identify an 
object. And you defined `@Propery` annotation to mark an objects named 
properties. You don't know the **origin** classes structure. One of the 
**origin** classes may look like in the following listing. 

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
implementations from.

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

// Yourr origin interface
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

### Installing Uniform Factory into your project

You can download **Uniform Factory** into your project from Maven Central.

Here is an example for Gradle

```
dependencies {
   compile group: 'com.github.antkudruk', name: 'uniform-factory', version: '0.1.2'
}
```

and for Maven

```
<dependency>
    <groupId>com.github.antkudruk</groupId>
    <artifactId>uniform-factory</artifactId>
    <version>0.1.2</version>
</dependency>
```

**Uniform Factory** is written to be applied in ByteBuddy Gradle Plugin to 
generate uniformed **wrapper** classes. Just import and apply  
`byte-buddy-gradle-plugin` and specify your plugin class.

Here is an example for Gradle

```
plugins {
    id 'java'
    id "net.bytebuddy.byte-buddy-gradle-plugin" version "1.10.6"
}

byteBuddy {
    transformation {
        plugin = // Specify reference to your plugin class here, see the next chapter
    }
}
```

and in Maven

```
    <plugin>
        <groupId>net.bytebuddy</groupId>
        <artifactId>byte-buddy-maven-plugin</artifactId>
        <version>1.10.6</version>
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

### Empty wrapper

Here is an example of **Wrapper** interface to be implemented by wrappers. It 
doesn't have any method so far and it's shown here just for the sake of 
demonstrating the concept. 

```
public interface Wrapper { }
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

We'll use *Byte buddy gradle plugin* to transform origin classes. The procedure 
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

Let me walk you through the constructor parameters.
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

Once the plugin has been implemented, you should refer it in 
`byteBuddy.transformation` property.
  
```
byteBuddy {
    transformation {
        plugin = 'PluginImpl'
    }
}
```

After applying the plugin each class marked with `@Marker` annotation will
be enhanced with `Wrapper` on the class loading. For instance, when you declare 
the following class

```
@Marker
public class OriginImpl { }
```

You'll be able to get the objects wrapper.

```
Object origin = new OriginImpl();
Wrapper wrapper = ((Origin)origin).getWrapper();
```

#### Using explicit interface to enhance objects with wrappers

You can avoid class cast made in the previous example the following way

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

#### Select type criteria

You may want to add wrappers to classes satisfying a custom criteria, for 
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

### Using Abstract Class as a Wrapper

Interfaces are stateless. You can't have any variables inside interfaces.

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

### Method Singleton

Let's enhance our empty `Wrapper` class.
Suppose we need each class annotated with `@Marker` annotation to provide it's
identity. However the classes are heterogeneous and we can't predict which 
values will be used as identities.  

First of all we should define a wrapper interface to get an identity as in the following listing.

```
    public interface Wrapper {
        String getId();
    }
```

Then we need to define an annotation to mark class members with to indicate the 
sources for identities.

```
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Identity { }
```

To make it work we have to specify the way of implementation of `getId()` 
method.

```
public class ClassFactoryGeneratorImpl extends DefaultMetaClassFactory<Wrapper> {
    public ClassFactoryGeneratorImpl() throws NoSuchMethodException {
        super(new ClassFactory.ShortcutBuilder<>(Wrapper.class)
            .addMethodSingleton(Wrapper.class.getMethod("getIdentity"), String.class)
            .setMarkerAnnotation(Identity.class)
            .addResultTranslator(Long.class, Object::toString)
            .endMethodDescription()
            .build());
    }
}
```

After that you can treat each class marked with `@Marker` annotation as a 
class implementing `Origin` interface and therefore get object identities 
in the common way. For example, if the classes have the following signature

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

you can access their name class members the following way.

```
Origin1 o1 = new Origin1();
Origin2 o2 = new Origin2();
        
assertEquals("10", ((Origin)o1).getWrapper().getIdentity());
assertEquals("name", ((Origin)o1).getWrapper().getIdentity())
``` 

#### Application: Tree

Let's consider an example.
Supposing we have the following structure of objects.
We have a Company. The company has some Departments and each department
has a list of employee.

```
@TreeElementMarker
public class Company {

    @Label
    private static final String companyName = "My awesome company";

    @Nested
    public List<Department> getDepartments() {
        return Arrays.asList(
                new Department("Managers", "Beavis", "Butthead"),
                new Department("Labours", "Stewart")
        );
    }
}

@TreeElementMarker
public class Department {

    @Label
    private final String depName;

    @Nested
    private final List<Employee> employee;

    public Department(String depName, String... employee) {
        this.depName = depName;
        this.employee = Stream.of(employee)
                .map(Employee::new)
                .collect(Collectors.toList());
    }
}

@TreeElementMarker
public class Employee {
    @Label
    private final String name;

    public Employee(String name) {
        this.name = name;
    }
}

```

To display this structure of object in the common way it's suitable to assign
a wrapper to each of the objects that will allow you to get nested elements and
labels for each element using the common interface

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

The example to get this structure is quite straightforward.
Note that in the following listing `nested()` method is assigned with default 
value. If no nested methods are defined in the origin class (like in Employee 
class in the listing above), the corresponding **wrapper** object will contain
an empty list of children.

```
public class ClassFactoryGeneratorImpl extends DefaultMetaClassFactory<TreeElement> {
    public ClassFactoryGeneratorImpl() throws ReflectiveOperationException {
        super(new ClassFactory.ShortcutBuilder<>(TreeElement.class)
                .addMethodSingleton(TreeElement.class.getMethod("getLabel"), String.class)
                .setMarkerAnnotation(Label.class)
                .endMethodDescription()

                .addMethodSingleton(TreeElement.class.getMethod("nested"), List.class)
                .setMarkerAnnotation(Nested.class)
                .setDefaultValue(new ArrayList<>())
                .endMethodDescription()

                .build());
    }
}
```

Once you used the classes above, you'll be able to iterate the elements like in
the following listing. Keep in mind that the method `nested()` returns list of 
**origin** objects implementing `HasTreeElement` interface, but not 
`TreeElement`. To access `TreeElement`, you should just call `getTreeElement()`
method explicitly.   

```
Company company = new Company();
        HasTreeElement tree = (HasTreeElement) company;
        assertEquals("Managers", tree.getTreeElement()
                .nested().get(0).getTreeElement().getLabel());
        assertEquals("Beavis", tree.getTreeElement()
                .nested().get(0).getTreeElement()
                .nested().get(0).getTreeElement().getLabel());
        assertEquals("Butthead", tree.getTreeElement()
                .nested().get(0).getTreeElement()
                .nested().get(1).getTreeElement().getLabel());
        assertEquals("Labours", tree.getTreeElement()
                .nested().get(1).getTreeElement().getLabel());
        assertEquals("Stewart", tree.getTreeElement()
                .nested().get(1).getTreeElement()
                .nested().get(0).getTreeElement().getLabel());
```

### Method List

Supposing we have callbacks in some classes. 
We'll mark the classes with `@Marker` annotation and processing methods with 
`@Callback` annotation. 

To call each callback method we'll use the following trick.
We'll introduce an additional wrapper interface to call each event handler in
the same way. Each method annotated with `@Processor.Process` annotation will
cause creation of a type implementing `Processor` interface. The method of that
interface will be invoking the corresponding origin method.

Note that the interface may contain one and only one method 

```
public interface Processor {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    @interface Process {

    }

    boolean process(String eventName);
}
```

Wrapper method will return list containing functional interface objects, in our 
case it will be a `List` of `Processor`s.   

```
public interface Wrapper {
    List<Processor> getProcessors();
}
```

To specify a way of implementing `getProcessors` method we'll use the same 
builder as we used for `MethodSingleton` specifying in `addMethodList` is the 
returning type of the **functional** interface.

```
public class ClassFactoryGeneratorImpl extends DefaultMetaClassFactory<Wrapper> {
    public ClassFactoryGeneratorImpl() throws NoSuchMethodException {
        this.classFactory = new ClassFactory.ShortcutBuilder<>(Wrapper.class)
                .addMethodList(
                        Wrapper.class.getMethod("getProcessors"),
                        boolean.class
                )
                .setMarkerAnnotation(Processor.Process.class)
                .setFunctionalInterface(Processor.class)
                .addResultTranslator(void.class, t -> true)
                .addResultTranslator(Long.class, t -> t >= 0)
                .addResultTranslator(String.class, "yes"::equalsIgnoreCase)
                .endMethodDescription()

                .build());
    }
}
```

In this case each *result mapper* should map an arbitary *origin method* 
return type to the method result in the *functional* interface.

### Implementing custom MetaclassGenerator

In the previous examples we used only default implementation 
`DefaultMetaClassFactory`, implementing interface `MetaClassFactory`.
`MetaClassFactory` instance is a singleton per application. 

Supposing, in the previous task we'd like to store a link to each object

Let's define custom `MetaClassFactory`.

```
public class ClassFactoryGeneratorImpl implements MetaClassFactory<Wrapper> {

    private final ClassFactory<Wrapper> classFactory;

    public ClassFactoryGeneratorImpl() throws NoSuchMethodException {
        this.classFactory = new ClassFactory.ShortcutBuilder<>(Wrapper.class)
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

### Method Map

Suppose we have objects that may be put into a specific coordinate.   

The wrapper should provide a method to return a map of coordinates using the 
same approach as we used for MethodList.

```
public interface Coordinate {
    long getCoordinate(Long scale);
}

public interface PointWrapper {
    Map<String, Coordinate> getCoords();
}
```

Unlikely for MethodList, MethodMap requires a rule to get the key in the map
based on the annotation. So we'll define a property (in this case default 
value) to define the key.

```
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface CoordinateMarker {
    String value();
}
```

And we'll describe the way of implementing that map.

```
public class ClassFactoryGeneratorImpl extends DefaultMetaClassFactory<PointWrapper> {
    public ClassFactoryGeneratorImpl() throws NoSuchMethodException {
        super(new ClassFactory.ShortcutBuilder<>(PointWrapper.class)

                .addMethodMap(PointWrapper.class.getMethod("getCoords"), long.class)
                .setMarkerAnnotation(CoordinateMarker.class, CoordinateMarker::value)
                .setFunctionalInterface(Coordinate.class)
                .parameterSource(Long.class, 0)

                .applyTo(new AnyParameterFilter())
                .addTranslator(boolean.class, t -> t > 0)
                .addTranslator(String.class, Object::toString)
                .addTranslator(long.class, t -> t)

                .finishParameterDescription()
                .addResultTranslator(Boolean.class, t -> t ? 1L : -1L)
                .addResultTranslator(String.class, Long::parseLong)
                .addResultTranslator(int.class, Integer::longValue)

                .endMethodDescription()

                .build());
    }
}
```

As a result you'll be able to get each axis position via that map

```
        PointTypeA pointTypeA = new PointTypeA();
        Point point = (Point) pointTypeA;

        point.getWrapper().getCoords().get("x").getCoordinate(10L);
        point.getWrapper().getCoords().get("y").getCoordinate(15L);
        point.getWrapper().getCoords().get("z").getCoordinate(20L);
```

You can use the following code to get `wrappers` generated behind the scenes.
To get it work just implement the following plugin.

```
public class MethodTreeWrapperClassFactory {

    private static final ClassFactory<Wrapper> classFactory;

    static  {
        try {
            classFactory = new ClassFactory.ShortcutBuilder<>(Wrapper.class)

                    .addMethodMap(Wrapper.class.getMethod("getWrappers"), String.class)
                    .setMarkerAnnotation(FunctionalElement.class, FunctionalElement::value)
                    .setFunctionalInterface(Fun.class)

                    .parameterSource(String.class, 0)
                    .applyToAnnotated(First.class)
                    .finishParameterDescription()

                    .parameterSource(String.class, 1)
                    .applyToAnnotated(Second.class)
                    .addTranslator(Boolean.class, "true"::equalsIgnoreCase)
                    .addTranslator(Long.class, Long::parseLong)
                    .finishParameterDescription()

                    .addResultTranslator(Long.class, t -> t.toString() + " units")
                    .addResultTranslator(Boolean.class, t -> t ? "Yes" : "No")
                    .endMethodDescription()

                    .build();
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public class PluginImpl extends WrapperPlugin<Wrapper> {
        public PluginImpl( ) {
            super(
                    Origin.class,
                    Wrapper.class,
                    Marker.class,
                    "examplePlugin",
                    ClassFactoryGeneratorImpl.class);
        }
    }

    public static class CtorMeta extends DefaultMetaClassFactory<Wrapper> {
        public CtorMeta() {
            super(classFactory);
        }
    }
}
```

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
        super(new ClassFactory.ShortcutBuilder<>(Wrapper.class)

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

## License

```
Copyright 2020 - 2021 Anton Kudruk

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
