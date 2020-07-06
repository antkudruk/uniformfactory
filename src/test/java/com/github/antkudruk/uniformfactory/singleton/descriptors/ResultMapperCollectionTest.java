package com.github.antkudruk.uniformfactory.singleton.descriptors;

import junit.framework.TestCase;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("WeakerAccess")
public class ResultMapperCollectionTest {

    private static final String ORIGIN_STRING_REPRESENTATION
            = "originStringRepresentation";

    public static class WrapperResultClass {

        private OriginResultClass origin;

        WrapperResultClass() {

        }

        WrapperResultClass(OriginResultClass origin) {
            this.origin = origin;
        }
    }

    public static class OriginResultClass {
        @Override
        public String toString() {
            return ORIGIN_STRING_REPRESENTATION;
        }
    }

    public static class OriginChildClass extends OriginResultClass {

    }

    @Test
    public void mapper() throws WrapperMethodTypesException {
        ResultMapperCollection<WrapperResultClass> resultMapperCollection
                = new ResultMapperCollection<>(WrapperResultClass.class);

        resultMapperCollection.addMapper(OriginResultClass.class, WrapperResultClass::new);

        Function<OriginResultClass, WrapperResultClass> translator
                = resultMapperCollection.getTranslatorOrThrow(
                new TypeDescription.ForLoadedType(OriginResultClass.class));

        OriginResultClass source = new OriginResultClass();
        assertEquals(source, translator.apply(source).origin);
    }

    @Test
    public void useChildrenOriginResultClasses() throws WrapperMethodTypesException {

        Function childResultTranslator = PowerMockito.mock(Function.class);

        @SuppressWarnings("unchecked")
        ResultMapperCollection<WrapperResultClass> resultMapperCollection
                = new ResultMapperCollection<>(WrapperResultClass.class)
                .addMapper(OriginResultClass.class, childResultTranslator);

        Function<OriginResultClass, WrapperResultClass> translator
                = resultMapperCollection.getTranslatorOrThrow(
                new TypeDescription.ForLoadedType(OriginChildClass.class));

        assertEquals(childResultTranslator, translator);
    }

    @Test
    public void skipParentOriginResultClasses() throws WrapperMethodTypesException {

        Function childResultTranslator = PowerMockito.mock(Function.class);
        Function parentResultTranslator = PowerMockito.mock(Function.class);

        @SuppressWarnings("unchecked")
        ResultMapperCollection<WrapperResultClass> resultMapperCollection
                = new ResultMapperCollection<>(WrapperResultClass.class)
                .addMapper(OriginChildClass.class, childResultTranslator)
                .addMapper(OriginResultClass.class, parentResultTranslator);

        Function<OriginResultClass, WrapperResultClass> translator
                = resultMapperCollection.getTranslatorOrThrow(
                new TypeDescription.ForLoadedType(OriginChildClass.class));

        assertEquals(parentResultTranslator, translator);
    }

    @Test
    public void useLastAddedTranslator() throws WrapperMethodTypesException {

        Function firstMapper = PowerMockito.mock(Function.class);
        Function secondMapper = PowerMockito.mock(Function.class);

        @SuppressWarnings("unchecked")
        ResultMapperCollection<WrapperResultClass> resultMapperCollection
                = new ResultMapperCollection<>(WrapperResultClass.class)
                .addMapper(OriginResultClass.class, firstMapper)
                .addMapper(OriginResultClass.class, secondMapper);

        Function<OriginResultClass, WrapperResultClass> translator
                = resultMapperCollection.getTranslatorOrThrow(
                new TypeDescription.ForLoadedType(OriginResultClass.class));

        assertEquals(secondMapper, translator);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void hasObjectToObjectTranslator() throws WrapperMethodTypesException {
        ResultMapperCollection<WrapperResultClass> resultMapperCollection
                = new ResultMapperCollection<>(WrapperResultClass.class);

        Function translator = resultMapperCollection.getTranslatorOrThrow(
                new TypeDescription.ForLoadedType(WrapperResultClass.class));

        WrapperResultClass source = new WrapperResultClass();
        assertEquals(source, translator.apply(source));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void translatorFromParentMapperTest() throws WrapperMethodTypesException {

        Function childResultTranslator = PowerMockito.mock(Function.class);
        Function parentResultTranslator = PowerMockito.mock(Function.class);

        ResultMapperCollection<WrapperResultClass> parentMapper
                = new ResultMapperCollection<>(WrapperResultClass.class);

        parentMapper.addMapper(String.class, parentResultTranslator);

        ResultMapperCollection<WrapperResultClass> childMapper = parentMapper.createChild();
        childMapper.addMapper(Long.class, childResultTranslator);

        Function suitableTranslator = childMapper.getTranslatorOrThrow(
                new TypeDescription.ForLoadedType(String.class));

        TestCase.assertEquals(parentResultTranslator, suitableTranslator);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void translatorFromChildMapperTest() throws WrapperMethodTypesException {

        Function childResultTranslator = PowerMockito.mock(Function.class);
        Function parentResultTranslator = PowerMockito.mock(Function.class);

        ResultMapperCollection<WrapperResultClass> parentMapper
                = new ResultMapperCollection<>(WrapperResultClass.class);

        parentMapper.addMapper(String.class, parentResultTranslator);

        ResultMapperCollection<WrapperResultClass> childMapper = parentMapper.createChild();
        childMapper.addMapper(Long.class, childResultTranslator);

        Function suitableTranslator = childMapper.getTranslatorOrThrow(
                new TypeDescription.ForLoadedType(Long.class));

        TestCase.assertEquals(childResultTranslator, suitableTranslator);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = WrapperMethodTypesException.class)
    public void translatorFromChildMapperNoTranslatorTest() throws WrapperMethodTypesException {

        Function childResultTranslator = PowerMockito.mock(Function.class);
        Function parentResultTranslator = PowerMockito.mock(Function.class);

        ResultMapperCollection<WrapperResultClass> parentMapper
                = new ResultMapperCollection<>(WrapperResultClass.class);

        parentMapper.addMapper(String.class, parentResultTranslator);

        ResultMapperCollection<WrapperResultClass> childMapper = parentMapper.createChild();
        childMapper.addMapper(Long.class, childResultTranslator);

        childMapper.getTranslatorOrThrow(new TypeDescription.ForLoadedType(Boolean.class));
    }
}
