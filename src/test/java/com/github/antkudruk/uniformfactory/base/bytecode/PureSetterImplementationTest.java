package com.github.antkudruk.uniformfactory.base.bytecode;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import static com.github.antkudruk.uniformfactory.common.TypeDescriptionShortcuts.findConstructor;
import static org.junit.Assert.assertEquals;

public class PureSetterImplementationTest {

    private static final String ORIGIN_FIELD = "origin";
    private static final String METHOD_NAME = "set";

    @SuppressWarnings("unused")
    public static class OriginImpl {

        public String publicStringField;

        private String privateStringField;

        public int publicIntValue;

        private int privateIntValue;

        public Integer publicIntegerValue;

        private Integer privateIntegerValue;
    }

    @Test
    public void givenPublicStringField_whenSet_applyParameter() throws Exception {
        givenField_whenSet_applyParameter("publicStringField", String.class, "value");
    }

    @Test
    public void givenPrivateStringField_whenSet_applyParameter() throws Exception {
        givenField_whenSet_applyParameter("privateStringField", String.class, "value");
    }

    @Test
    public void givenPublicIntegerField_whenSet_applyParameter() throws Exception {
        givenField_whenSet_applyParameter("publicIntegerValue", Integer.class, 13);
    }

    @Test
    public void givenPrivateIntegerField_whenSet_applyParameter() throws Exception {
        givenField_whenSet_applyParameter("privateIntegerValue", Integer.class, 13);
    }

    @Test
    public void givenPublicIntField_whenSet_applyParameter() throws Exception {
        givenField_whenSet_applyParameter("publicIntValue", Integer.class, 13);
    }

    @Test
    public void givenPrivateIntField_whenSet_applyParameter() throws Exception {
        givenField_whenSet_applyParameter("privateIntValue", Integer.class, 13);
    }


    private void givenField_whenSet_applyParameter(String fieldName, Class<?> argumentType, Object value) throws Exception {
        // Given
        FieldDescription.InDefinedShape valueField = new TypeDescription.ForLoadedType(OriginImpl.class)
                .getDeclaredFields()
                .filter(ElementMatchers.named(fieldName))
                .getOnly();

        // When
        Implementation implementation = new PureSetterImplementation(ORIGIN_FIELD, valueField);

        ByteBuddy byteBuddy = new ByteBuddy();
        Class wrapper = byteBuddy.subclass(Object.class)
                .defineConstructor(Visibility.PUBLIC)
                .withParameters(OriginImpl.class)
                .intercept(MethodCall.invoke(findConstructor(Object.class).orElse(null))
                        .andThen(FieldAccessor.ofField(ORIGIN_FIELD).setsArgumentAt(0)))
                .defineField(ORIGIN_FIELD, OriginImpl.class, Opcodes.ACC_PRIVATE)
                .defineMethod(METHOD_NAME, void.class, Opcodes.ACC_PUBLIC)
                .withParameters(argumentType)
                .intercept(implementation)
                .make()
                .load(getClass().getClassLoader())
                .getLoaded();

        OriginImpl origin = new OriginImpl();
        Object o = wrapper.getConstructor(OriginImpl.class).newInstance(origin);

        // Then
        Whitebox.invokeMethod(o, METHOD_NAME, value);
        assertEquals(value, Whitebox.getInternalState(Whitebox.getInternalState(o, ORIGIN_FIELD), fieldName));
    }
}
