package com.github.antkudruk.uniformfactory.methodlist;

import com.github.antkudruk.uniformfactory.methodlist.descriptors.MethodListDescriptor;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialMapperImpl;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.ParameterValue;
import com.github.antkudruk.uniformfactory.base.exception.WrongTypeException;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.AnnotationParameterFilter;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static junit.framework.TestCase.assertEquals;

public class MethodListTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    public @interface MethodMarker {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface Name {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface Index {
    }

    @SuppressWarnings("unused")
    public static class OriginImpl {
        @MethodMarker
        public String field = "Epsilon";

        @MethodMarker
        public String getNumberA(@Name String a, @Index String b) {
            return a + " " + b;
        }

        @MethodMarker
        public String getNumberB(@Name String a) {
            return a + " " + a;
        }

        @MethodMarker
        public String getNumberB() {
            return "Empty";
        }

        @MethodMarker
        public String getNumberD(@Name String a, @Index Integer b) {
            return a + " " + b;
        }
    }

    public interface Fun {
        String getId(String a, Long b);
    }

    public interface Wrapper {
        List<Fun> getFunctionsList();
    }

    public interface WrapperOfMaps {
        Map<String, Fun> getFunctionsList();
    }

    @Test
    public void test() throws ReflectiveOperationException, ClassGeneratorException {
        // given
        OriginImpl origin = new OriginImpl();

        ClassFactory<Wrapper> classFactory = new ClassFactory.Builder<>(Wrapper.class)
                .addMethodDescriptor(
                        new MethodListDescriptor.Builder<>(
                                    Fun.class,
                                    Wrapper.class.getMethod("getFunctionsList"))
                                .defaultElementSource()

                                .setMarkerAnnotation(MethodMarker.class)
                                .getterElementFactory(String.class)
                                .parameterSource(String.class, 0)
                                .applyToAnnotated(Name.class)
                                .finishParameterDescription()

                                .parameterSource(Long.class, 1)
                                .applyToAnnotated(Index.class)
                                .addTranslator(Integer.class, Long::intValue)
                                .finishParameterDescription()
                                .finishElementFactory()

                                .endElementSource()
                                .build()
                )
                .build();

        // when
        Function<OriginImpl, Wrapper> wrapperFactory = classFactory.buildWrapperFactory(OriginImpl.class);
        Wrapper wrapper = wrapperFactory.apply(origin);
        List<Fun> map = wrapper.getFunctionsList();

        // then
        Set<String> actual = map
                .stream()
                .map(t -> t.getId("Foo", 10L))
                .collect(Collectors.toSet());

        Set<String> expected = Stream.of("Epsilon",
                "Foo 10",
                "Foo Foo",
                "Empty",
                "Epsilon").collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @Test
    public void test1() throws ReflectiveOperationException, ClassGeneratorException {
        // given
        OriginImpl origin = new OriginImpl();

        ClassFactory<Wrapper> classFactory = new ClassFactory.Builder<>(Wrapper.class)
                .addMethodDescriptor(new MethodListDescriptor.Builder<>(
                        Fun.class, Wrapper.class.getMethod("getFunctionsList"))
                        .defaultElementSource()

                        .setMarkerAnnotation(MethodMarker.class)
                        .getterElementFactory(String.class)
                        .constantSource("Value")
                        .applyToTyped(String.class)

                        .constantSource("Test")
                        .applyToAnnotated(Name.class)

                        .constantSource(10)
                        .applyToAnnotated(Index.class)

                        .finishElementFactory()
                        .endElementSource()

                        .build()
                )
                .build();

        // when
        Function<OriginImpl, Wrapper> wrapperFactory = classFactory.buildWrapperFactory(OriginImpl.class);
        Wrapper wrapper = wrapperFactory.apply(origin);
        List<Fun> map = wrapper.getFunctionsList();

        // then
        Set<String> actual = map
                .stream()
                .map(t -> t.getId("Foo", 10L))
                .collect(Collectors.toSet());

        Set<String> expected = Stream.of("Epsilon",
                "Test Value",
                "Test Test",
                "Empty",
                "Test 10",
                "Epsilon").collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @Test
    public void simpleBuilderTest() throws ReflectiveOperationException, ClassGeneratorException {
        // given
        OriginImpl origin = new OriginImpl();

        ClassFactory<Wrapper> classFactory = new ClassFactory.Builder<>(Wrapper.class)
                .addMethodDescriptor(
                        new MethodListDescriptor.Builder<>(
                                Fun.class,
                                Wrapper.class.getMethod("getFunctionsList"))

                                .defaultElementSource()

                                .setMarkerAnnotation(MethodMarker.class)

                                .getterElementFactory(String.class)

                                .addParameterTranslator(new PartialMapperImpl(
                                        new AnnotationParameterFilter<>(Name.class),
                                        new ParameterValue<>(String.class, 0)
                                ))

                                .addParameterTranslator(new PartialMapperImpl(
                                        new AnnotationParameterFilter<>(Index.class),
                                        new ParameterValue<>(Long.class, 1)
                                                .addTranslatorForExtends(new TypeDescription.ForLoadedType(Integer.class), Long::intValue)
                                ))

                                .finishElementFactory()

                                .endElementSource()

                                .build()
                )
                .build();

        // when
        Function<OriginImpl, Wrapper> wrapperFactory = classFactory.buildWrapperFactory(OriginImpl.class);
        Wrapper wrapper = wrapperFactory.apply(origin);
        List<Fun> map = wrapper.getFunctionsList();

        // then
        Set<String> actual = map
                .stream()
                .map(t -> t.getId("Foo", 10L))
                .collect(Collectors.toSet());

        Set<String> expected = Stream.of("Epsilon",
                "Foo 10",
                "Foo Foo",
                "Empty",
                "Epsilon").collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @Test(expected = WrongTypeException.class)
    public void inappropriateMethodReturnType() throws ReflectiveOperationException {
        new MethodListDescriptor.Builder<>(Fun.class, WrapperOfMaps.class.getMethod("getFunctionsList"))
                .build();
    }
}
