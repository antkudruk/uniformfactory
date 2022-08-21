package com.github.antkudruk.uniformfactory.stackmanipulation;

import com.github.antkudruk.uniformfactory.base.MethodDescriptor;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@SuppressWarnings("unused")
public class BbImplementationMethodDescriptorTest {

    private static final String HELLO = "Hello";

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Marker {

    }

    public interface AdaptorWithString {
        String get();
    }

    public interface AdaptorWithPrimitive {
        int get();
    }

    public interface AdaptorWithField {
        Field get();
    }

    public interface AdaptorWithMethod {
        Method get();
    }

    public static class SampleMethodDelegation {
        public static String sayHello() {
            return HELLO;
        }
    }

    public static class OriginWithMethodAndField {
        private String field;

        public String get() {
            throw new RuntimeException("Not implemented");
        }
    }

    private <T, O> T getClassFactory(
            Class<T> adapterClass,
            MethodDescriptor descriptor,
            Class<O> originClass
    ) throws ReflectiveOperationException, ClassGeneratorException {
        O origin = originClass.getConstructor().newInstance();
        ClassFactory<T> classFactory = new ClassFactory.Builder<>(adapterClass)
                .addMethodDescriptor(descriptor)
                .build();
        Class<? extends T> resultClass = classFactory
                .build(new TypeDescription.ForLoadedType(originClass))
                .load(getClass().getClassLoader())
                .getLoaded();
        return resultClass.getConstructor(originClass).newInstance(origin);
    }

    @Test
    public void whenOk_thenOk() throws ReflectiveOperationException, ClassGeneratorException {
        // when
        AdaptorWithString obj = getClassFactory(
                AdaptorWithString.class,
                new BbImplementationMethodDescriptor
                        .Builder(AdaptorWithString.class.getMethod("get"))
                        .setImplementation(MethodDelegation.to(SampleMethodDelegation.class))
                        .build(),
                Object.class
        );

        // then
        assertEquals(HELLO, obj.get());
    }

    @Test
    public void whenNullMethod_thenNullImplementation() throws ReflectiveOperationException, ClassGeneratorException {
        // when
        AdaptorWithMethod obj = getClassFactory(
                AdaptorWithMethod.class,
                new BbImplementationMethodDescriptor
                        .Builder(AdaptorWithMethod.class.getMethod("get"))
                        .methodConstant(null)
                        .build(),
                OriginWithMethodAndField.class
        );

        // then
        assertNull(obj.get());
    }

    @Test
    public void whenNullField_thenNullImplementation() throws ReflectiveOperationException, ClassGeneratorException {
        // when
        AdaptorWithField obj = getClassFactory(
                AdaptorWithField.class,
                new BbImplementationMethodDescriptor
                        .Builder(AdaptorWithField.class.getMethod("get"))
                        .fieldConstant(null)
                        .build(),
                OriginWithMethodAndField.class
        );

        // then
        assertNull(obj.get());
    }

    @Test
    public void whenMethod_thenReturnMethod() throws ReflectiveOperationException, ClassGeneratorException {
        // when
        AdaptorWithMethod obj = getClassFactory(
                AdaptorWithMethod.class,
                new BbImplementationMethodDescriptor
                        .Builder(AdaptorWithMethod.class.getMethod("get"))
                        .methodConstant(
                                new TypeDescription.ForLoadedType(OriginWithMethodAndField.class)
                                        .getDeclaredMethods()
                                        .filter(ElementMatchers.named("get"))
                                        .getOnly()
                        )
                        .build(),
                OriginWithMethodAndField.class
        );

        // then
        assertEquals(OriginWithMethodAndField.class.getMethod("get"), obj.get());
    }

    @Test
    public void whenField_thenReturnField() throws ReflectiveOperationException, ClassGeneratorException {
        // when
        AdaptorWithField obj = getClassFactory(
                AdaptorWithField.class,
                new BbImplementationMethodDescriptor
                        .Builder(AdaptorWithField.class.getMethod("get"))
                        .fieldConstant(
                                new TypeDescription.ForLoadedType(OriginWithMethodAndField.class)
                                        .getDeclaredFields()
                                        .filter(ElementMatchers.named("field"))
                                        .getOnly()
                        )
                        .build(),
                OriginWithMethodAndField.class
        );

        // then
        assertEquals(OriginWithMethodAndField.class.getDeclaredField("field"), obj.get());
    }

    @Test
    public void whenNullImplementation_thenReturnNull() throws ReflectiveOperationException, ClassGeneratorException {
        // when
        AdaptorWithString obj = getClassFactory(
                AdaptorWithString.class,
                new BbImplementationMethodDescriptor
                        .Builder(AdaptorWithString.class.getMethod("get"))
                        .nullConstant()
                        .build(),
                Object.class
        );

        // then
        assertNull(obj.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenWrongReturnType_thenThrowException() throws ReflectiveOperationException, ClassGeneratorException {
        // when
        getClassFactory(
                AdaptorWithPrimitive.class,
                new BbImplementationMethodDescriptor
                        .Builder(AdaptorWithPrimitive.class.getMethod("get"))
                        .setImplementation(MethodDelegation.to(SampleMethodDelegation.class))
                        .build(),
                Object.class);
    }
}
