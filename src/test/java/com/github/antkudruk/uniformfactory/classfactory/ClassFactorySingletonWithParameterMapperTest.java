package com.github.antkudruk.uniformfactory.classfactory;

import com.github.antkudruk.uniformfactory.singleton.descriptors.MethodSingletonDescriptor;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static junit.framework.TestCase.assertEquals;

public class ClassFactorySingletonWithParameterMapperTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    @interface MethodMarker {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface First {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface Second {

    }

    public interface Wrapper {
        String common(String a, String b);
    }

    public static class OriginConcatTwoStrings {
        @SuppressWarnings("unused")
        @MethodMarker
        public String concat(@First String s0, @Second String s1) {
            return s0 + " " + s1;
        }
    }

    public static class OriginConcatStringAndLong {
        @SuppressWarnings("unused")
        @MethodMarker
        public String concat(@First String s0, @Second Long s1) {
            return s0 + " " + s1;
        }
    }

    public static class OriginConcatStringAndBoolean {
        @SuppressWarnings("unused")
        @MethodMarker
        public String concat(@First String s0, @Second Boolean s1) {
            return s0 + " " + s1;
        }
    }

    @Test
    public void test() throws ReflectiveOperationException, ClassGeneratorException {
        ClassFactory<Wrapper> classFactory = createClassFactory();
        Class<? extends Wrapper> wrapperClass = classFactory
                .build(new TypeDescription.ForLoadedType(OriginConcatTwoStrings.class))
                .load(getClass().getClassLoader())
                .getLoaded();

        OriginConcatTwoStrings origin = new OriginConcatTwoStrings();
        Wrapper w = wrapperClass.getConstructor(OriginConcatTwoStrings.class).newInstance(origin);
        assertEquals("Hello World", w.common("Hello", "World"));
    }

    @Test
    public void test1() throws ReflectiveOperationException, ClassGeneratorException {
        ClassFactory<Wrapper> classFactory = createClassFactory();
        Class<? extends Wrapper> wrapperClass = classFactory
                .build(new TypeDescription.ForLoadedType(OriginConcatStringAndLong.class))
                .load(getClass().getClassLoader())
                .getLoaded();

        OriginConcatStringAndLong origin = new OriginConcatStringAndLong();
        Wrapper w = wrapperClass.getConstructor(OriginConcatStringAndLong.class).newInstance(origin);
        assertEquals("Hello 10", w.common("Hello", "10"));
    }

    @Test
    public void test2() throws ReflectiveOperationException, ClassGeneratorException {
        ClassFactory<Wrapper> classFactory = createClassFactory();
        Class<? extends Wrapper> wrapperClass = classFactory
                .build(new TypeDescription.ForLoadedType(OriginConcatStringAndBoolean.class))
                .load(getClass().getClassLoader())
                .getLoaded();

        OriginConcatStringAndBoolean origin = new OriginConcatStringAndBoolean();
        Wrapper w = wrapperClass.getConstructor(OriginConcatStringAndBoolean.class).newInstance(origin);
        assertEquals("Hello false", w.common("Hello", "false"));
    }

    @Test
    public void test3() throws ReflectiveOperationException, ClassGeneratorException {
        ClassFactory<Wrapper> classFactory = createClassFactory();
        Class<? extends Wrapper> wrapperClass = classFactory
                .build(new TypeDescription.ForLoadedType(OriginConcatStringAndBoolean.class))
                .load(getClass().getClassLoader())
                .getLoaded();

        OriginConcatStringAndBoolean origin = new OriginConcatStringAndBoolean();
        Wrapper w = wrapperClass.getConstructor(OriginConcatStringAndBoolean.class).newInstance(origin);
        assertEquals("Hello true", w.common("Hello", "TrUe"));
    }

    private ClassFactory<Wrapper> createClassFactory() throws ReflectiveOperationException {
        return new ClassFactory.Builder<>(Wrapper.class)
                .addMethodDescriptor(
                        new MethodSingletonDescriptor.Builder<>(
                                Wrapper.class.getMethod("common", String.class, String.class),
                                String.class
                        )
                                .setMarkerAnnotation(MethodMarker.class)
                                .parameterSource(String.class, 0)
                                .applyToAnnotated(First.class)
                                .finishParameterDescription()

                                .parameterSource(String.class, 1)
                                .applyToAnnotated(Second.class)
                                .addTranslator(Boolean.class, "true"::equalsIgnoreCase)
                                .addTranslator(Long.class, Long::parseLong)
                                .finishParameterDescription()

                                .build()
                )
                .build();
    }
}
