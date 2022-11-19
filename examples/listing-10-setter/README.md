# Setter

Supposing, you need to set up annotated field of a class, no matter what type 
or what name the field has. How can you do that?

You can use MethodDescriptor for setter:

```java
public class ClassFactoryGeneratorImpl extends DefaultMetaClassFactory<Adapter> {
    public ClassFactoryGeneratorImpl() throws ReflectiveOperationException {
        super(new ClassFactory.Builder<>(Adapter.class)
                .addMethodDescriptor(
                        new SetterDescriptor.Builder<>(
                                Adapter.class.getDeclaredMethod("setValue", String.class))
                                .setAnnotation(Marker.class)
                                .parameterSource(String.class, 0)
                                .applyToAny()
                                .addTranslator(Integer.class, Integer::parseInt)
                                .finishParameterDescription()
                                .build()
                )
                .build());
    }
}
```

Uniform Factory implements a method that consumes only one parameter. This 
parameter has exactly the same type as the field has. The only thing the
method does is assigning the value of the method parameter to the field. 

Then the rules for applying parameters works on this simple method,
exactly the same way as 
[MethodSingleton](https://github.com/antkudruk/uniformfactory/tree/develop/examples/listing-3-method-singleton)
does.

Thus, mapper for parameter types works the same way as for 
MethodSingleton.

