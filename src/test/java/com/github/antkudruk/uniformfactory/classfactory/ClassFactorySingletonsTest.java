package com.github.antkudruk.uniformfactory.classfactory;

import com.github.antkudruk.uniformfactory.singleton.descriptors.MethodSingletonDescriptor;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ClassFactorySingletonsTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    public @interface Marker {

    }

    public interface Wrapper {
        String getIdentity();
    }

    @SuppressWarnings("WeakerAccess")
    public static class JustOrigin {
    }

    public static class OriginWithMethod {
        @SuppressWarnings("unused")
        @Marker
        public Long getId() {
            return 10L;
        }
    }

    public static class OriginWithField {
        @SuppressWarnings("unused")
        @Marker
        public Long id = 20L;
    }

    @Test
    public void testMethod() throws ReflectiveOperationException, ClassGeneratorException {
        ClassFactory<Wrapper> classFactory = getClassFactory();

        Class<? extends Wrapper> wrapperClass = classFactory
                .build(new TypeDescription.ForLoadedType(OriginWithMethod.class))
                .load(getClass().getClassLoader())
                .getLoaded();

        OriginWithMethod origin = new OriginWithMethod();
        Wrapper wrapper = wrapperClass.getConstructor(OriginWithMethod.class).newInstance(origin);

        assertEquals("10", wrapper.getIdentity());

    }

    @Test
    public void testField() throws ReflectiveOperationException, ClassGeneratorException {
        ClassFactory<Wrapper> classFactory = getClassFactory();

        Class<? extends Wrapper> wrapperClass = classFactory
                .build(new TypeDescription.ForLoadedType(OriginWithField.class))
                .load(getClass().getClassLoader())
                .getLoaded();

        OriginWithField origin = new OriginWithField();
        Wrapper wrapper = wrapperClass.getConstructor(OriginWithField.class).newInstance(origin);

        assertEquals("20", wrapper.getIdentity());
    }

    @Test(expected = RuntimeException.class)
    public void testEmpty() throws ReflectiveOperationException, ClassGeneratorException {
        ClassFactory<Wrapper> classFactory = getClassFactory();

        classFactory
                .build(new TypeDescription.ForLoadedType(JustOrigin.class))
                .load(getClass().getClassLoader())
                .getLoaded();
    }

    @Test
    public void testDefaultValue() throws ReflectiveOperationException, ClassGeneratorException {
        ClassFactory<Wrapper> classFactory = getClassFactoryWithDefault("default");

        Class<? extends Wrapper> wrapperClass = classFactory
                .build(new TypeDescription.ForLoadedType(JustOrigin.class))
                .load(getClass().getClassLoader())
                .getLoaded();

        JustOrigin origin = new JustOrigin();
        Wrapper wrapper = wrapperClass.getConstructor(JustOrigin.class).newInstance(origin);

        assertEquals("default", wrapper.getIdentity());
    }

    @Test
    public void testDefaultNull() throws ReflectiveOperationException, ClassGeneratorException {
        ClassFactory<Wrapper> classFactory = getClassFactoryWithDefault(null);

        Class<? extends Wrapper> wrapperClass = classFactory
                .build(new TypeDescription.ForLoadedType(JustOrigin.class))
                .load(getClass().getClassLoader())
                .getLoaded();

        JustOrigin origin = new JustOrigin();
        Wrapper wrapper = wrapperClass.getConstructor(JustOrigin.class).newInstance(origin);

        assertNull(wrapper.getIdentity());
    }

    private ClassFactory<Wrapper> getClassFactory() throws NoSuchMethodException {
        return new ClassFactory.Builder<>(Wrapper.class)
                .addMethodDescriptor(new MethodSingletonDescriptor.Builder<>(
                        Wrapper.class.getMethod("getIdentity"),
                        String.class)
                        .setMarkerAnnotation(Marker.class)
                        .addResultTranslator(Long.class, Object::toString)
                        .build()
                )
                .build();
    }

    private ClassFactory<Wrapper> getClassFactoryWithDefault(String defaultValue) throws NoSuchMethodException {
        return new ClassFactory.Builder<>(Wrapper.class)
                .addMethodDescriptor(new MethodSingletonDescriptor.Builder<>(
                        Wrapper.class.getMethod("getIdentity"),
                        String.class)
                        .setMarkerAnnotation(Marker.class)
                        .addResultTranslator(Long.class, Object::toString)
                        .setDefaultValue(defaultValue)
                        .build()
                )
                .build();
    }
}
