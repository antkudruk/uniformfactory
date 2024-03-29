package com.github.antkudruk.uniformfactory.classfactory;

import com.github.antkudruk.uniformfactory.exception.AlienMethodException;
import org.junit.Test;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("unused")
public class ClassFactoryTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface Marker {

    }

    public interface Wrapper {
        String concat(String first, Long value);
    }

    public interface WrapperWithOrigin {
        Object getOrigin();
    }

    private interface PrivateWrapper {
        String concat(String first, Long value);
    }

    static class Alien {
        public String concat(String first, Long value) {
            throw new NotImplementedException();
        }
    }

    public static class WrapperImpl implements Wrapper {
        @Override
        public String concat(String first, Long value) {
            throw new NotImplementedException();
        }
    }

    @Test
    public void givenOrigin_whenNotDescribed_thenOk() {
        new ClassFactory.Builder<>(WrapperWithOrigin.class)
                .build();
    }

    @Test(expected = AlienMethodException.class)
    public void givenAlienMethod_whenNew_thenThrow() throws ReflectiveOperationException {
        new ClassFactory.Builder<>(Wrapper.class)
                .addMethodSingleton(
                        Alien.class.getDeclaredMethod("concat", String.class, Long.class),
                        String.class)
                .setMarkerAnnotation(Marker.class)
                .endMethodDescription()
                .build();
    }

    @Test
    public void givenOriginMethod_whenNew_thenOk() throws ReflectiveOperationException {
        new ClassFactory.Builder<>(Wrapper.class)
                .addMethodSingleton(
                        Wrapper.class.getDeclaredMethod("concat", String.class, Long.class),
                        String.class)
                .setMarkerAnnotation(Marker.class)
                .endMethodDescription()
                .build();
    }

    @Test
    public void givenBaseMethod_whenNew_thenOk() throws ReflectiveOperationException {
        new ClassFactory.Builder<>(WrapperImpl.class)
                .addMethodSingleton(
                        Wrapper.class.getDeclaredMethod("concat", String.class, Long.class),
                        String.class)
                .setMarkerAnnotation(Marker.class)
                .endMethodDescription()
                .build();
    }

    @Test(expected = WrapperInterfaceIsNotPublic.class)
    public void givenWrapperInterfaceNotPublic_whenNew_thenThrow() throws ReflectiveOperationException {
        new ClassFactory.Builder<>(PrivateWrapper.class)
                .addMethodSingleton(
                        PrivateWrapper.class.getDeclaredMethod("concat", String.class, Long.class),
                        String.class)
                .setMarkerAnnotation(Marker.class)
                .endMethodDescription()
                .build();
    }
}
