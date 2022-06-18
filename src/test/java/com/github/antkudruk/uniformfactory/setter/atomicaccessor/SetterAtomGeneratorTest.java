package com.github.antkudruk.uniformfactory.setter.atomicaccessor;

import com.github.antkudruk.uniformfactory.setter.atomicaccassor.SetterAtomGenerator;
import com.github.antkudruk.uniformfactory.singleton.argument.exceptions.ParameterTranslatorNotFound;
import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.AnyParameterFilter;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialMapperImpl;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialParameterUnion;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.ParameterValue;
import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.Constants;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class SetterAtomGeneratorTest {

    private static final String NEW_VALUE_SETTER = "New Value";
    private static final String TRANSLATED_NEW_VALUE = "Translated New Value";

    public static class OriginImpl {
        public String field = "Initial";
    }

    public interface Wrapper {
        void setField(String value);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void setFieldUsingTranslator() throws ReflectiveOperationException, ParameterTranslatorNotFound {
        Class atomClass = SetterAtomGenerator.INSTANCE.generateClass(
                    new TypeDescription.ForLoadedType(OriginImpl.class),
                    Wrapper.class.getDeclaredMethod("setField", String.class),
                        new PartialParameterUnion.Builder()
                                .add(
                                        new PartialMapperImpl(
                                                new AnyParameterFilter(),
                                                new ParameterValue<>(String.class, 0)
                                                        .addTranslator(new TypeDescription.ForLoadedType(String.class), s -> TRANSLATED_NEW_VALUE)
                                        )
                                )
                                .build(),
                    new FieldDescription.ForLoadedField(
                            OriginImpl.class.getField("field")))
                .load(getClass().getClassLoader())
                .getLoaded();

        OriginImpl origin = new OriginImpl();

        Method method = atomClass.getDeclaredMethod(Constants.METHOD_NAME, String.class);
        Object atom = atomClass.getConstructor(OriginImpl.class).newInstance(origin);

        method.invoke(atom, NEW_VALUE_SETTER);

        Object result = Whitebox.getInternalState(origin, "field");

        // Then
        assertEquals(TRANSLATED_NEW_VALUE, result);
    }
}
