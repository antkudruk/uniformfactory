package com.github.antkudruk.uniformfactory.methodmap;

import com.github.antkudruk.uniformfactory.base.exception.WrongTypeException;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.methodmap.descriptors.MethodMapDescriptor;
import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.AnnotationParameterFilter;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialMapperImpl;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.ParameterValue;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static junit.framework.TestCase.assertEquals;

public class MethodMapTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    public @interface MethodMarker {
        String value();
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
        @MethodMarker("epsilon")
        public String field = "Epsilon";

        @MethodMarker("alpha")
        public String getNumberA(@Name String a, @Index String b) {
            return a + " " + b;
        }

        @MethodMarker("beta")
        public String getNumberB(@Name String a) {
            return a + " " + a;
        }

        @MethodMarker("gamma")
        public String getNumberB() {
            return "Empty";
        }

        @MethodMarker("delta")
        public String getNumberD(@Name String a, @Index Integer b) {
            return a + " " + b;
        }
    }

    public interface Fun {
        String getId(String a, Long b);
    }

    public interface Wrapper {
        Map<String, Fun> getFunctionsList();
    }

    public interface WrapperOfList {
        List<Fun> getFunctionsList();
    }

    @Test
    public void test() throws ReflectiveOperationException, ClassGeneratorException {
        OriginImpl origin = new OriginImpl();
        ClassFactory<Wrapper> classFactory = new ClassFactory.Builder<>(Wrapper.class)
                .addMethodDescriptor(
                        new MethodMapDescriptor.Builder<>(Fun.class,
                                Wrapper.class.getMethod("getFunctionsList"))
                                .annotationMapElementSource()
                                .setMarkerAnnotation(MethodMarker.class, MethodMarker::value)
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
        Map<String, Fun> map = wrapper.getFunctionsList();

        // then
        assertEquals("Epsilon", map.get("epsilon").getId("Foo", 10L));
        assertEquals("Foo 10", map.get("alpha").getId("Foo", 10L));
        assertEquals("Foo Foo", map.get("beta").getId("Foo", 10L));
        assertEquals("Empty", map.get("gamma").getId("Foo", 10L));
        assertEquals("Foo 10", map.get("delta").getId("Foo", 10L));
        assertEquals("Epsilon", map.get("epsilon").getId("Foo", 10L));
    }

    @Test
    public void test1() throws ReflectiveOperationException, ClassGeneratorException {
        // given
        OriginImpl origin = new OriginImpl();

        ClassFactory<Wrapper> classFactory = new ClassFactory.Builder<>(Wrapper.class)
                .addMethodDescriptor(new MethodMapDescriptor.Builder<>(
                        Fun.class,
                        Wrapper.class.getMethod("getFunctionsList"))

                        .annotationMapElementSource()

                        .setMarkerAnnotation(MethodMarker.class, MethodMarker::value)

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
        Map<String, Fun> map = wrapper.getFunctionsList();

        // then
        assertEquals("Test Value", map.get("alpha").getId("Foo", 10L));
        assertEquals("Test Test", map.get("beta").getId("Foo", 10L));
        assertEquals("Empty", map.get("gamma").getId("Foo", 10L));
        assertEquals("Test 10", map.get("delta").getId("Foo", 10L));
        assertEquals("Epsilon", map.get("epsilon").getId("Foo", 10L));
    }

    @Test
    public void simpleBuilderTest() throws ReflectiveOperationException, ClassGeneratorException {
        // given
        OriginImpl origin = new OriginImpl();

        ClassFactory<Wrapper> classFactory = new ClassFactory.Builder<>(Wrapper.class)
                .addMethodDescriptor(
                        new MethodMapDescriptor.Builder<>(
                                Fun.class,
                                Wrapper.class.getMethod("getFunctionsList"))

                                .annotationMapElementSource()

                                .setMarkerAnnotation(MethodMarker.class, MethodMarker::value)

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
        Map<String, Fun> map = wrapper.getFunctionsList();

        // then
        assertEquals("Epsilon", map.get("epsilon").getId("Foo", 10L));
        assertEquals("Foo 10", map.get("alpha").getId("Foo", 10L));
        assertEquals("Foo Foo", map.get("beta").getId("Foo", 10L));
        assertEquals("Empty", map.get("gamma").getId("Foo", 10L));
        assertEquals("Foo 10", map.get("delta").getId("Foo", 10L));
        assertEquals("Epsilon", map.get("epsilon").getId("Foo", 10L));
    }

    @Test(expected = WrongTypeException.class)
    public void inappropriateMethodReturnType() throws ReflectiveOperationException {
        new MethodMapDescriptor.Builder<>(
                Fun.class,
                WrapperOfList.class.getMethod("getFunctionsList")
        ).build();
    }
}
