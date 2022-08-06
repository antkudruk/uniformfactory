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

    public static class OriginWithPrimitiveImpl {
        public int field = 13;
    }

    public static class OriginWithPrivateStringField {
        private String field = "Initial";
    }

    public static class OriginWithPrivatePrimitiveImpl {
        private int field = 13;
    }

    public interface Wrapper {
        void setField(String value);
    }

    private Class generateWrapperClass(Class originClass) throws ReflectiveOperationException, ParameterTranslatorNotFound {
        return SetterAtomGenerator.INSTANCE.generateClass(
                        new TypeDescription.ForLoadedType(originClass),
                        Wrapper.class.getDeclaredMethod("setField", String.class),
                        new PartialParameterUnion.Builder()
                                .add(
                                        new PartialMapperImpl(
                                                new AnyParameterFilter(),
                                                new ParameterValue<>(String.class, 0)
                                                        .addTranslator(new TypeDescription.ForLoadedType(String.class), s -> TRANSLATED_NEW_VALUE)
                                                        .addTranslator(new TypeDescription.ForLoadedType(int.class), Integer::parseInt)
                                        )
                                )
                                .build(),
                        new FieldDescription.ForLoadedField(originClass.getDeclaredField("field")))
                .load(getClass().getClassLoader())
                .getLoaded();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void setFieldUsingTranslator() throws ReflectiveOperationException, ParameterTranslatorNotFound {
        Class atomClass = generateWrapperClass(OriginImpl.class);
        OriginImpl origin = new OriginImpl();

        Method method = atomClass.getDeclaredMethod(Constants.METHOD_NAME, String.class);
        Object atom = atomClass.getConstructor(OriginImpl.class).newInstance(origin);

        method.invoke(atom, NEW_VALUE_SETTER);

        Object result = Whitebox.getInternalState(origin, "field");

        // Then
        assertEquals(TRANSLATED_NEW_VALUE, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void setPrimitiveFieldUsingTranslator() throws ReflectiveOperationException, ParameterTranslatorNotFound {
        Class atomClass = generateWrapperClass(OriginWithPrimitiveImpl.class);
        OriginWithPrimitiveImpl origin = new OriginWithPrimitiveImpl();

        Object atom = atomClass
                .getConstructor(OriginWithPrimitiveImpl.class)
                .newInstance(origin);
        Method method = atomClass.getDeclaredMethod(Constants.METHOD_NAME, String.class);

        method.invoke(atom, "13");

        Object result = Whitebox.getInternalState(origin, "field");

        // Then
        assertEquals(13, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void setPrivateFieldUsingTranslator() throws ReflectiveOperationException, ParameterTranslatorNotFound {
        Class atomClass = generateWrapperClass(OriginWithPrivateStringField.class);
        OriginWithPrivateStringField origin = new OriginWithPrivateStringField();

        Method method = atomClass.getDeclaredMethod(Constants.METHOD_NAME, String.class);
        Object atom = atomClass.getConstructor(OriginWithPrivateStringField.class).newInstance(origin);

        method.invoke(atom, NEW_VALUE_SETTER);

        Object result = Whitebox.getInternalState(origin, "field");

        // Then
        assertEquals(TRANSLATED_NEW_VALUE, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void setPrivatePrimitiveFieldUsingTranslator() throws ReflectiveOperationException, ParameterTranslatorNotFound {
        Class atomClass = generateWrapperClass(OriginWithPrivatePrimitiveImpl.class);
        OriginWithPrivatePrimitiveImpl origin = new OriginWithPrivatePrimitiveImpl();

        Object atom = atomClass
                .getConstructor(OriginWithPrivatePrimitiveImpl.class)
                .newInstance(origin);
        Method method = atomClass.getDeclaredMethod(Constants.METHOD_NAME, String.class);

        method.invoke(atom, "13");

        Object result = Whitebox.getInternalState(origin, "field");

        // Then
        assertEquals(13, result);
    }
}
