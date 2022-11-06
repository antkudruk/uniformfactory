package com.github.antkudruk.uniformfactory.singleton.argument.typemapper;

import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.function.Function;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

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

    @SuppressWarnings({"unchecked", "OptionalGetWithoutIsPresent"})
    @Test
    public void givenInheritedParameter_whenFindSuitableTranslator_thenChooseAppropriateTranslatorFor() {

        // given
        ParameterMappersCollection<ParameterClass> mapper
                = new ParameterMappersCollection<>(ParameterClass.class);

        TypeDescription parentParameterTypeDescription = new TypeDescription.ForLoadedType(
                ParentParameterClass.class);

        Function translator = mockRepeater();

        mapper = mapper.add(parentParameterTypeDescription, translator);

        // when
        ParameterMappersCollection.ParameterMapperDescriptor<ParameterClass>
                suitableTranslator = mapper.findSuitableTranslator(parentParameterTypeDescription).get();

        // then
        assertEquals(translator, suitableTranslator.getTranslator());
    }

    @SuppressWarnings({"unchecked", "OptionalGetWithoutIsPresent"})
    @Test
    public void givenFewTranslator_whenFindSuitableTranslator_thenLastSuitableTranslator() {

        // given
        ParameterMappersCollection<ParameterClass> mapper
                = new ParameterMappersCollection<>(ParameterClass.class);

        TypeDescription targetTypeDescription = new TypeDescription.ForLoadedType(ParameterClass.class);

        Function firstTranslator = mockRepeater();
        Function lastTranslator = mockRepeater();

        mapper.add(targetTypeDescription, firstTranslator);
        mapper.add(targetTypeDescription, lastTranslator);

        // when
        ParameterMappersCollection.ParameterMapperDescriptor<ParameterClass>
                suitableTranslator = mapper.findSuitableTranslator(targetTypeDescription).get();

        // then
        assertNotEquals(lastTranslator, firstTranslator);
        assertEquals(lastTranslator, suitableTranslator.getTranslator());
        assertNotEquals(firstTranslator, suitableTranslator.getTranslator());
    }

    private <I, O> Function<I, O> mockRepeater() {
        //noinspection unchecked
        return Mockito.mock(Function.class);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void hasRepeaterByDefault() {

        ParameterMappersCollection<ParameterClass> mapper
                = new ParameterMappersCollection<>(ParameterClass.class);

        TypeDescription targetType = new TypeDescription.ForLoadedType(ParameterClass.class);

        ParameterMappersCollection.ParameterMapperDescriptor<ParameterClass>
                suitableTranslator = mapper.findSuitableTranslator(targetType).get();

        ParameterClass parameter = new ParameterClass();

        assertEquals(parameter, suitableTranslator.getTranslator().apply(parameter));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void hasToStringByDefault() {
        ParameterMappersCollection<ParameterClass> mapper
                = new ParameterMappersCollection<>(ParameterClass.class);

        TypeDescription targetType = new TypeDescription.ForLoadedType(String.class);

        ParameterMappersCollection.ParameterMapperDescriptor<ParameterClass>
                suitableTranslator = mapper.findSuitableTranslator(targetType).get();

        ParameterClass parameter = new ParameterClass();

        assertEquals(PARAMETER_CLASS_STRING_REPRESENTATION,
                suitableTranslator.getTranslator().apply(parameter));
    }

    @Test
    public void absentParameterTypeCausesException() {
        ParameterMappersCollection<ParameterClass> mapper
                = new ParameterMappersCollection<>(ParameterClass.class);

        TypeDescription notDefinedTargetType = new TypeDescription.ForLoadedType(NotDefinedParameterClass.class);

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
        parentMapper = parentMapper.add(parentParameterTypeDescription, parentTranslator);

        ParameterMappersCollection<ParameterClass> childMapper = parentMapper.createChild();
        Function<ParameterClass, ?> childTranslator = mockRepeater();
        childMapper.add(wrongTypeDescription, childTranslator);

        // when
        Function<?, ?> suitableTranslator = childMapper
                .findSuitableTranslator(parentParameterTypeDescription)
                .map(ParameterMappersCollection.ParameterMapperDescriptor::getTranslator)
                .orElseThrow(RuntimeException::new);

        // then
        assertEquals(parentTranslator, suitableTranslator);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void translatorFromChildMapperTest() {
        // given
        ParameterMappersCollection<ParameterClass> parentMapper
                = new ParameterMappersCollection<>(ParameterClass.class);

        TypeDescription parentParameterTypeDescription = new TypeDescription.ForLoadedType(
                ParentParameterClass.class);

        Function parentTranslator = mockRepeater();
        parentMapper = parentMapper.add(parentParameterTypeDescription, parentTranslator);

        ParameterMappersCollection<ParameterClass> childMapper = parentMapper.createChild();
        Function childTranslator = mockRepeater();
        childMapper.add(parentParameterTypeDescription, childTranslator);

        // when
        ParameterMappersCollection.ParameterMapperDescriptor<ParameterClass>
                suitableTranslator = childMapper
                .findSuitableTranslator(parentParameterTypeDescription)
                .orElseThrow(RuntimeException::new);
        // then
        assertEquals(childTranslator, suitableTranslator.getTranslator());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void translatorNoMapperTest() {

        ParameterMappersCollection<ParameterClass> parentMapper
                = new ParameterMappersCollection<>(ParameterClass.class);

        TypeDescription parentParameterTypeDescription = new TypeDescription.ForLoadedType(
                ParentParameterClass.class);

        Function parentTranslator = mockRepeater();
        parentMapper = parentMapper.add(parentParameterTypeDescription, parentTranslator);

        ParameterMappersCollection<ParameterClass> childMapper = parentMapper.createChild();
        Function childTranslator = mockRepeater();
        childMapper.add(parentParameterTypeDescription, childTranslator);

        Optional<ParameterMappersCollection.ParameterMapperDescriptor<ParameterClass>>
                suitableTranslator = childMapper.findSuitableTranslator(new TypeDescription.ForLoadedType(boolean.class));

        assertFalse(suitableTranslator.isPresent());
    }
}
