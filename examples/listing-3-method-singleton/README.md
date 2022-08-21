# Method Singleton

## Example Structure

There example contains two projects:
 
  * `plugin` stands for the example of framework
  * `client` application that uses (in our cse just tests) the framework
 
The project `plugen` is not aware about the project `client`. Thus, you can
apply `plugin` to any other application.

### Plugin Project 

Here is an example of using 
[Uniform Factory](https://github.com/antkudruk/uniformfactory) to generate 
method singleton.

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
        super(new ClassFactory.Builder<>(Wrapper.class)
            .addMethodSingleton(Wrapper.class.getMethod("getIdentity"), String.class)
            .setMarkerAnnotation(Identity.class)
            .addResultTranslator(Long.class, Object::toString)
            .endMethodDescription()
            .build());
    }
}
```

### Client Application Example

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