package com.github.antkudruk.uniformfactory.singleton.argument.typemapper;

import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Function;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;

@SuppressWarnings("WeakerAccess")
public class ParameterMappersCollectionTest {

    private static final String PARAMETER_CLASS_STRING_REPRESENTATION
            = "parameterClass test object";

    public static class AlienParameterClass {
    }

    public static class ParentParameterClass {
    }

    public static class ParameterClass extends ParentParameterClass {
        @Override
        public String toString() {
            return PARAMETER_CLASS_STRING_REPRESENTATION;
        }
    }

    public static class NotDefinedParameterClass {

    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent"})
    @Test
    public void givenInheritedParameter_whenFindSuitableTranslator_thenChooseAppropriateTranslatorFor() {

        // given
        ParameterMappersCollection<ParameterClass> mapper
                = new ParameterMappersCollection<>(ParameterClass.class);

        TypeDescription parentParameterTypeDescription = new TypeDescription.ForLoadedType(
                ParentParameterClass.class);

        Function<ParameterClass, ?> translator = mockRepeater();

        mapper = mapper.add(new ExtendsParameterTranslator<>(parentParameterTypeDescription, translator));

        // when
        Function<ParameterClass, ?>
                suitableTranslator = mapper.findSuitableTranslator(parentParameterTypeDescription).get();

        // then
        assertEquals(translator, suitableTranslator);
    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent"})
    @Test
    public void givenFewTranslator_whenFindSuitableTranslator_thenLastSuitableTranslator() {

        // given
        ParameterMappersCollection<ParameterClass> mapper
                = new ParameterMappersCollection<>(ParameterClass.class);

        TypeDescription targetTypeDescription = new TypeDescription.ForLoadedType(ParameterClass.class);

        Function<ParameterClass, ?> firstTranslator = mockRepeater();
        Function<ParameterClass, ?> lastTranslator = mockRepeater();

        mapper.add(new ExtendsParameterTranslator<>(targetTypeDescription, firstTranslator));
        mapper.add(new ExtendsParameterTranslator<>(targetTypeDescription, lastTranslator));

        // when
        Function<ParameterClass, ?>
                suitableTranslator = mapper.findSuitableTranslator(targetTypeDescription).get();

        // then
        assertNotEquals(lastTranslator, firstTranslator);
        assertEquals(lastTranslator, suitableTranslator);
        assertNotEquals(firstTranslator, suitableTranslator);
    }

    private <I, O> Function<I, O> mockRepeater() {
        //noinspection unchecked
        return mock(Function.class);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void hasRepeaterByDefault() {
        // given
        ParameterMappersCollection<ParameterClass> mapper
                = new ParameterMappersCollection<>(ParameterClass.class);

        TypeDescription targetType = new TypeDescription.ForLoadedType(ParameterClass.class);

        Function<ParameterClass, ?>
                suitableTranslator = mapper.findSuitableTranslator(targetType).get();

        // when
        ParameterClass parameter = new ParameterClass();

        // then
        assertEquals(parameter, suitableTranslator.apply(parameter));
    }

    @Test
    public void hasSuperObjectByDefault() {
        // given
        ParameterMappersCollection<Object> mapper
                = new ParameterMappersCollection<>(Object.class);

        TypeDescription targetType = new TypeDescription.ForLoadedType(ParameterClass.class);

        Function<Object, ?>
                suitableTranslator = mapper.findSuitableTranslator(targetType).orElseThrow(RuntimeException::new);

        // when
        ParameterClass parameter = new ParameterClass();

        // then
        assertEquals(parameter, suitableTranslator.apply(parameter));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void hasToStringByDefault() {
        ParameterMappersCollection<ParameterClass> mapper
                = new ParameterMappersCollection<>(ParameterClass.class);

        TypeDescription targetType = new TypeDescription.ForLoadedType(String.class);

        Function<ParameterClass, ?>
                suitableTranslator = mapper.findSuitableTranslator(targetType).get();

        ParameterClass parameter = new ParameterClass();

        assertEquals(PARAMETER_CLASS_STRING_REPRESENTATION,
                suitableTranslator.apply(parameter));
    }

    @Test
    public void absentParameterTypeCausesException() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        // given
        ParameterMappersCollection<ParameterClass> mapper
                = (ParameterMappersCollection<ParameterClass>)Whitebox.getConstructor(ParameterMappersCollection.class,
                        Class.class, ParameterMappersCollection.class)
                .newInstance(ParameterClass.class, null);

        TypeDescription notDefinedTargetType = new TypeDescription.ForLoadedType(Object.class);

        // when/then
        assertFalse(mapper.findSuitableTranslator(notDefinedTargetType).isPresent());
    }

    @Test
    public void givenNoTypeDescription_whenFindSuitableTranslator_thenUseParentTranslator() {
        // given
        ParameterMappersCollection<ParameterClass> parentMapper
                = new ParameterMappersCollection<>(ParameterClass.class);

        TypeDescription parentParameterTypeDescription = new TypeDescription.ForLoadedType(
                ParentParameterClass.class);

        TypeDescription wrongTypeDescription = new TypeDescription.ForLoadedType(AlienParameterClass.class);

        Function<ParameterClass, ?> parentTranslator = mockRepeater();
        parentMapper = parentMapper.add(new ExtendsParameterTranslator<>(parentParameterTypeDescription, parentTranslator));

        ParameterMappersCollection<ParameterClass> childMapper = parentMapper.createChild();
        Function<ParameterClass, ?> childTranslator = mockRepeater();
        childMapper.add(new ExtendsParameterTranslator<>(wrongTypeDescription, childTranslator));

        // when
        Function<?, ?> suitableTranslator = childMapper
                .findSuitableTranslator(parentParameterTypeDescription)
                .orElseThrow(RuntimeException::new);

        // then
        assertEquals(parentTranslator, suitableTranslator);
    }

    @Test
    public void translatorFromChildMapperTest() {
        // given
        ParameterMappersCollection<ParameterClass> parentMapper
                = new ParameterMappersCollection<>(ParameterClass.class);

        TypeDescription parentParameterTypeDescription = new TypeDescription.ForLoadedType(
                ParentParameterClass.class);

        Function<ParameterClass, ?> parentTranslator = mockRepeater();
        parentMapper = parentMapper.add(new ExtendsParameterTranslator<>(parentParameterTypeDescription, parentTranslator));

        ParameterMappersCollection<ParameterClass> childMapper = parentMapper.createChild();
        Function<ParameterClass, ?> childTranslator = mockRepeater();
        childMapper.add(new ExtendsParameterTranslator<>(parentParameterTypeDescription, childTranslator));

        // when
        Function<ParameterClass, ?>
                suitableTranslator = childMapper
                .findSuitableTranslator(parentParameterTypeDescription)
                .orElseThrow(RuntimeException::new);
        // then
        assertEquals(childTranslator, suitableTranslator);
    }

    @Test
    public void translatorNoMapperTest() {

        ParameterMappersCollection<ParameterClass> parentMapper
                = new ParameterMappersCollection<>(ParameterClass.class);

        TypeDescription parentParameterTypeDescription = new TypeDescription.ForLoadedType(
                ParentParameterClass.class);

        Function<ParameterClass, ?> parentTranslator = mockRepeater();
        parentMapper = parentMapper.add(new ExtendsParameterTranslator<>(parentParameterTypeDescription, parentTranslator));

        ParameterMappersCollection<ParameterClass> childMapper = parentMapper.createChild();
        Function<ParameterClass, ?> childTranslator = mockRepeater();
        childMapper.add(new ExtendsParameterTranslator<>(parentParameterTypeDescription, childTranslator));

        // when
        Optional<Function<ParameterClass, ?>>
                suitableTranslator = childMapper.findSuitableTranslator(new TypeDescription.ForLoadedType(boolean.class));

        // then
        ParameterClass parameterClass = new ParameterClass();
        assertEquals(parameterClass, suitableTranslator.map(e -> parameterClass).orElseThrow(RuntimeException::new));
    }
}
