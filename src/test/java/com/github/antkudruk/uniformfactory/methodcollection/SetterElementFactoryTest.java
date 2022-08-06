package com.github.antkudruk.uniformfactory.methodcollection;

import com.github.antkudruk.uniformfactory.util.FakePartialDescriptor;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.singleton.argument.exceptions.ParameterTranslatorNotFound;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.ParameterBindersSource;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

@SuppressWarnings("UnusedReturnValue")
public class SetterElementFactoryTest {

    public interface FunReturningVoid {
        void set();
    }


    public interface FunReturningAtomic {
        int set();
    }

    public interface FunReturningReference {
        Integer set();
    }

    public static class Origin {
        @SuppressWarnings("unused")
        private String stringField;
    }

    private static final TypeDescription ORIGIN_TYPE_DESCRIPTION = new TypeDescription.ForLoadedType(Origin.class);

    private <T> T getElementForType(Class<T> funType, Origin origin) throws ClassGeneratorException, ReflectiveOperationException {
        ParameterBindersSource partialParameterUnion = mock(ParameterBindersSource.class);
        when(partialParameterUnion.getParameterBinders(any()))
                .thenReturn(Collections.singletonList(new FakePartialDescriptor<>(0, "stringValue")));

        ElementFactory<T> setterElementFactory =
                new SetterElementFactory.Builder<>(funType)
                        .setParameterMapper(partialParameterUnion)
                        .build();

        Class<? extends T> elementType = setterElementFactory.getFieldElement(
                        ORIGIN_TYPE_DESCRIPTION,
                        ORIGIN_TYPE_DESCRIPTION
                                .getDeclaredFields()
                                .filter(ElementMatchers.named("stringField"))
                                .getOnly()
                ).build(ORIGIN_TYPE_DESCRIPTION)
                .load(getClass().getClassLoader())
                .getLoaded();

        return elementType.getConstructor(Origin.class).newInstance(origin);
    }

    @Test
    public void givenFunReturningVoid_whenGetFieldElement() throws ClassGeneratorException, ReflectiveOperationException {
        // given
        Origin origin = new Origin();
        FunReturningVoid element = getElementForType(FunReturningVoid.class, origin);

        // when
        element.set();

        // then
        assertEquals("stringValue", Whitebox.getInternalState(origin, "stringField"));
    }

    @Test
    public void givenFunReturningReference_whenGetFieldElement() throws ClassGeneratorException, ReflectiveOperationException {
        // given
        Origin origin = new Origin();
        FunReturningReference element = getElementForType(FunReturningReference.class, origin);

        // when
        element.set();

        // then
        assertEquals("stringValue", Whitebox.getInternalState(origin, "stringField"));
    }

    @Test
    public void givenFunReturningAtomic_whenGetFieldElement() throws ClassGeneratorException, ReflectiveOperationException {
        // given
        Origin origin = new Origin();
        FunReturningAtomic element = getElementForType(FunReturningAtomic.class, origin);

        // when
        element.set();

        // then
        assertEquals("stringValue", Whitebox.getInternalState(origin, "stringField"));
    }

    @Test(expected = ParameterTranslatorNotFound.class)
    public void givenNoParameterTranslator_whenGetFieldElement_thenThrowParameterTranslatorNotFound()
            throws ClassGeneratorException {
        // given
        ParameterBindersSource partialParameterUnion = Mockito.mock(ParameterBindersSource.class);
        when(partialParameterUnion.getParameterBinders(any())).thenThrow(ParameterTranslatorNotFound.class);
        ElementFactory<FunReturningVoid> setterElementFactory =
                new SetterElementFactory.Builder<>(FunReturningVoid.class)
                        .setParameterMapper(partialParameterUnion)
                        .build();

        // when
        setterElementFactory.getFieldElement(
                ORIGIN_TYPE_DESCRIPTION,
                ORIGIN_TYPE_DESCRIPTION
                        .getDeclaredFields()
                        .filter(ElementMatchers.named("stringField"))
                        .getOnly()
        ).build(ORIGIN_TYPE_DESCRIPTION);
    }
}
