package com.github.antkudruk.uniformfactory.methodcollection;

import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.singleton.argument.exceptions.ParameterTranslatorNotFound;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.ParameterBindersSource;
import com.github.antkudruk.uniformfactory.singleton.descriptors.ResultMapperCollection;
import com.github.antkudruk.uniformfactory.singleton.descriptors.WrapperMethodTypesException;
import com.github.antkudruk.uniformfactory.util.FakePartialDescriptor;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Test;
import org.mockito.Answers;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Generic test used for the different element types
 *
 * @param <F> Element type
 * @param <R> Method result type
 */
@RequiredArgsConstructor
public abstract class GetterElementFactoryForElementTypeTest<F, R> {

    public static final String STRING_FIELD_VALUE = "Origin field value";
    public static final int PRIMITIVE_INT_VALUE = 13;

    private final Class<F> elementType;
    private final Class<R> resultType;
    private final R result;

    @SuppressWarnings("unused")
    public static class Origin {

        private String stringField = STRING_FIELD_VALUE;
        private int primitiveField = PRIMITIVE_INT_VALUE;

        public String methodReturningString(Long first, String second, Boolean third) {
            return Stream.of(first, second, third)
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
        }

        public Long methodRetuningLong(Long first, String second, Boolean third) {
            return 13L;
        }

        public long methodRetuningLongPrimitive(long first, String second, boolean third) {
            return 13L;
        }

        public void methodRetuningVoid(long first, String second, boolean third) {

        }
    }

    private static final TypeDescription ORIGIN_TYPE_DESCRIPTION = new TypeDescription.ForLoadedType(Origin.class);

    public ElementFactory<F> getElementFactory(ResultMapperCollection<R> resultMapperCollection)
            throws ParameterTranslatorNotFound {
        return
                new GetterElementFactory.Builder<>(elementType, resultType)
                        .setParameterMapper(getUnionReturningExactParams(10L, "stringValue", true))
                        .setResultMapper(resultMapperCollection)
                        .build();
    }

    private Class<? extends F> getElementTypeForMethod(
            String methodName,
            ResultMapperCollection<R> resultMapperCollection) throws ClassGeneratorException {

        ElementFactory<F> elementFactory = getElementFactory(resultMapperCollection);

        return elementFactory.getMethodElement(
                        ORIGIN_TYPE_DESCRIPTION,
                        ORIGIN_TYPE_DESCRIPTION.getDeclaredMethods().filter(ElementMatchers.named(methodName)).getOnly()
                ).build(ORIGIN_TYPE_DESCRIPTION)
                .load(getClass().getClassLoader())
                .getLoaded();
    }

    private Class<? extends F> getElementTypeForField(
            String fieldName,
            ResultMapperCollection<R> resultMapperCollection) throws ClassGeneratorException {

        ElementFactory<F> elementFactory = getElementFactory(resultMapperCollection);

        return elementFactory.getFieldElement(
                        ORIGIN_TYPE_DESCRIPTION,
                        ORIGIN_TYPE_DESCRIPTION.getDeclaredFields().filter(ElementMatchers.named(fieldName)).getOnly()
                ).build(ORIGIN_TYPE_DESCRIPTION)
                .load(getClass().getClassLoader())
                .getLoaded();
    }

    @Test
    public void givenOriginReturningString_whenGetMethodElement() throws ClassGeneratorException, ReflectiveOperationException {
        // given
        Origin origin = new Origin();

        // when
        Class<? extends F> elementType = getElementTypeForMethod(
                "methodReturningString",
                getResultMapperCollection(result, "10, stringValue, true"));

        // then
        F element = elementType.getConstructor(Origin.class).newInstance(origin);
        R result = callMethod(element);
        assertEquals(result, result);
    }

    @Test
    public void givenOriginReturningLong_whenGetMethodElement() throws ClassGeneratorException, ReflectiveOperationException {
        // given
        Origin origin = new Origin();

        // when
        Class<? extends F> elementType = getElementTypeForMethod(
                "methodRetuningLong",
                getResultMapperCollection(result, 13L));

        // then
        F element = elementType.getConstructor(Origin.class).newInstance(origin);
        R result = callMethod(element);
        assertEquals(result, result);
    }

    @Test
    public void givenOriginReturningPrimitive_whenGetMethodElement() throws ClassGeneratorException, ReflectiveOperationException {
        // given
        Origin origin = new Origin();

        // when
        Class<? extends F> elementType = getElementTypeForMethod(
                "methodRetuningLongPrimitive",
                getResultMapperCollection(result, 13L));

        // then
        F element = elementType.getConstructor(Origin.class).newInstance(origin);
        R result = callMethod(element);
        assertEquals(this.result, result);
    }

    @Test
    public void givenOriginStringField_whenGetFieldElement() throws ClassGeneratorException, ReflectiveOperationException {
        // given
        Origin origin = new Origin();

        // when
        Class<? extends F> elementType = getElementTypeForField(
                "stringField",
                getResultMapperCollection(result, STRING_FIELD_VALUE));

        // then
        F element = elementType.getConstructor(Origin.class).newInstance(origin);
        R result = callMethod(element);
        assertEquals(result, result);
    }

    @Test
    public void givenOriginPrimitiveField_whenGetFieldElement() throws ClassGeneratorException, ReflectiveOperationException {
        // given
        Origin origin = new Origin();

        // when
        Class<? extends F> elementType = getElementTypeForField(
                "primitiveField",
                getResultMapperCollection(result, PRIMITIVE_INT_VALUE));

        // then
        F element = elementType.getConstructor(Origin.class).newInstance(origin);
        R result = callMethod(element);
        assertEquals(result, result);
    }

    protected abstract R callMethod(F element);

    public ParameterBindersSource getUnionReturningExactParams(Object... params) throws ParameterTranslatorNotFound {
        ParameterBindersSource partialParameterUnion = mock(ParameterBindersSource.class);
        when(partialParameterUnion.getParameterBinders(any()))
                .thenReturn(
                        IntStream.range(0, params.length)
                                .boxed()
                                .map(i -> new FakePartialDescriptor<>(i, params[i]))
                                .collect(Collectors.toList()));
        return partialParameterUnion;
    }

    public ResultMapperCollection<R> getResultMapperCollection(
            R resultValue,
            Object inputValue) throws WrapperMethodTypesException {

        Function<Object, R> translator = mock(Function.class);
        when(translator.apply(eq(inputValue))).thenReturn(resultValue);

        ResultMapperCollection<R> resultMapperCollection = mock(ResultMapperCollection.class, Answers.RETURNS_DEEP_STUBS);
        when(resultMapperCollection.getTranslatorOrThrow(any()))
                .thenReturn(r -> {
                    return translator.apply(r);
                });
        when(resultMapperCollection.getWrapperReturnType()).thenReturn(resultType);
        when(resultMapperCollection.createChild()).thenReturn(resultMapperCollection);
        return resultMapperCollection;
    }
}
