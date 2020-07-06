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

public class ClassFactoryTwoSingletonsTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    public @interface First {

    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    public @interface Second {

    }

    public interface Wrapper {
        String getFirst();
        String getSecond();
    }

    public static class OriginWithMethod {
        @SuppressWarnings("unused")
        @First
        public Long getFirst() {
            return 10L;
        }

        @SuppressWarnings("unused")
        @Second
        public Long getSecond() {
            return 20L;
        }
    }

    public static class OriginWithField {
        @SuppressWarnings("unused")
        @First
        public Long first = 10L;

        @SuppressWarnings("unused")
        @Second
        public Long second = 20L;
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

        assertEquals("10", wrapper.getFirst());

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

        assertEquals("10", wrapper.getFirst());
    }

    private ClassFactory<Wrapper> getClassFactory() throws NoSuchMethodException {
        return new ClassFactory.Builder<>(Wrapper.class)
                .addMethodDescriptor(new MethodSingletonDescriptor.Builder<>(
                        First.class,
                        Wrapper.class.getMethod("getFirst"),
                        String.class)
                        .addResultTranslator(Long.class, Object::toString)
                        .build()
                )
                .addMethodDescriptor(new MethodSingletonDescriptor.Builder<>(
                        Second.class,
                        Wrapper.class.getMethod("getSecond"),
                        String.class)
                        .addResultTranslator(Long.class, Object::toString)
                        .build()
                )
                .build();
    }
}
