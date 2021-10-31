package com.github.antkudruk.uniformfactory.setter.atomicaccessor;

import com.github.antkudruk.uniformfactory.setter.atomicaccassor.SetterAtomGenerator;
import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.Constants;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Method;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

public class SetterAtomGeneratorTest {

    private static final String NEW_VALUE_SETTER = "New Value";
    private static final String TRANSLATED_NEW_VALUE = "Translated New Value";

    public static class OriginImpl {
        public String field = "Initial";
    }

    @Test
    @SuppressWarnings("unchecked")
    public void setFieldUsingTranslator() throws ReflectiveOperationException {
        Function resultTranslatorMock = Mockito.mock(Function.class);
        Mockito.when(resultTranslatorMock.apply(eq(NEW_VALUE_SETTER))).thenReturn(TRANSLATED_NEW_VALUE);

        Class atomClass = SetterAtomGenerator.INSTANCE.generateClass(
                new TypeDescription.ForLoadedType(OriginImpl.class),
                resultTranslatorMock,
                new FieldDescription.ForLoadedField(
                        OriginImpl.class.getField("field")))
                .load(getClass().getClassLoader())
                .getLoaded();

        OriginImpl origin = new OriginImpl();

        Method method = atomClass.getDeclaredMethod(Constants.METHOD_NAME, Object.class);
        Object atom = atomClass.getConstructor(OriginImpl.class).newInstance(origin);

        method.invoke(atom, NEW_VALUE_SETTER);

        Object result = Whitebox.getInternalState(origin, "field");

        // Then
        Mockito.verify(resultTranslatorMock, times(1))
                .apply(NEW_VALUE_SETTER);

        assertEquals(TRANSLATED_NEW_VALUE, result);
    }
}
