package com.github.antkudruk.uniformfactory.base.bytecode;

import com.github.antkudruk.uniformfactory.common.TypeDescriptionShortcuts;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import static junit.framework.TestCase.assertEquals;

public class SetFieldImplementationTest {

    private static final String ORIGIN_FIELD = "originField";
    private static final String METHOD_NAME = "set";

    public static class OriginImpl {
        @SuppressWarnings("unused")
        public String publicField;
        @SuppressWarnings("unused")
        private String privateField;
    }

    public static class OriginWithIntImpl {
        @SuppressWarnings("unused")
        public Integer boxedPublicField;
        @SuppressWarnings("unused")
        public int unboxedPublicField;
        @SuppressWarnings("unused")
        private Integer boxedPrivateField;
        @SuppressWarnings("unused")
        private int unboxedPrivateField;
    }

    @SuppressWarnings("WeakerAccess")
    public static class OriginDerived extends OriginImpl {
    }

    @Test
    public void testPublic() throws Exception {
        testPublisAndPrivate(OriginImpl.class, "publicField", "10");
    }

    @Test
    public void testPrivate() throws Exception {
        testPublisAndPrivate(OriginImpl.class, "privateField", "20");
    }

    @Test
    public void testSuperPublic() throws Exception {
        testPublisAndPrivate(OriginDerived.class, "publicField", "10");
    }

    @Test
    public void testSuperPrivate() throws Exception {
        testPublisAndPrivate(OriginDerived.class, "privateField", "20");
    }

    @SuppressWarnings("unchecked")
    private void testPublisAndPrivate(Class originClass, String fieldName, Object expectedResult) throws Exception {

        TypeDescription originTypeDescription
                = new TypeDescription.ForLoadedType(originClass);

        ByteBuddy byteBuddy = new ByteBuddy();

        Class<?> wrapperClass = byteBuddy
                .subclass(Object.class)
                .defineField(ORIGIN_FIELD, originClass, Opcodes.ACC_PRIVATE)
                .defineMethod(METHOD_NAME, void.class, Opcodes.ACC_PUBLIC)
                .withParameters(String.class)
                .intercept(new SetFieldImplementation(ORIGIN_FIELD,
                        TypeDescriptionShortcuts.deepFindField(originTypeDescription, fieldName)
                                .orElseThrow(RuntimeException::new),
                        t -> t
                ))

                .make()
                .load(getClass().getClassLoader())
                .getLoaded();

        Object origin = originClass.getConstructor().newInstance();
        Object wrapper = wrapperClass.getConstructor().newInstance();
        Whitebox.setInternalState(wrapper, ORIGIN_FIELD, origin);
        Whitebox.invokeMethod(wrapper, METHOD_NAME, expectedResult);
        assertEquals(expectedResult, Whitebox.getInternalState(origin, fieldName));
    }

    @Test
    public void testPublicTransformingValue() throws Exception {

        TypeDescription originTypeDescription
                = new TypeDescription.ForLoadedType(OriginImpl.class);

        ByteBuddy byteBuddy = new ByteBuddy();

        Class<?> wrapperClass = byteBuddy
                .subclass(Object.class)
                .defineField(ORIGIN_FIELD, OriginImpl.class, Opcodes.ACC_PRIVATE)
                .defineMethod(METHOD_NAME, void.class, Opcodes.ACC_PUBLIC)
                .withParameters(Object.class)
                .intercept(new SetFieldImplementation(ORIGIN_FIELD,
                        originTypeDescription
                                .getDeclaredFields()
                                .filter(ElementMatchers.named("publicField"))
                                .getOnly(),
                        t -> t.toString() + t.toString()
                ))
                .make()
                .load(getClass().getClassLoader())
                .getLoaded();

        Object wrapper = wrapperClass.getConstructor().newInstance();
        OriginImpl origin = new OriginImpl();
        Whitebox.setInternalState(wrapper, ORIGIN_FIELD, origin);
        Whitebox.invokeMethod(wrapper, METHOD_NAME, "10");

        assertEquals("1010", Whitebox.getInternalState(origin, "publicField"));
    }

    @Test
    public void testIntegerBoxed() throws Exception {
        testBoxedAndUnboxed(OriginWithIntImpl.class, "boxedPublicField", Integer.valueOf(10));
    }

    @Test
    public void testIntegerUnboxed() throws Exception {
        testBoxedAndUnboxed(OriginWithIntImpl.class, "unboxedPublicField", 10);
    }

    @Test
    public void testPrivateIntegerBoxed() throws Exception {
        testBoxedAndUnboxed(OriginWithIntImpl.class, "boxedPrivateField", Integer.valueOf(10));
    }

    @Test
    public void testPrivateIntegerUnboxed() throws Exception {
        testBoxedAndUnboxed(OriginWithIntImpl.class, "unboxedPrivateField", 10);
    }

    @SuppressWarnings("unchecked")
    private void testBoxedAndUnboxed(Class originClass, String fieldName, Object expectedResult) throws Exception {

        TypeDescription originTypeDescription
                = new TypeDescription.ForLoadedType(originClass);

        ByteBuddy byteBuddy = new ByteBuddy();

        Class<?> wrapperClass = byteBuddy
                .subclass(Object.class)
                .defineField(ORIGIN_FIELD, originClass, Opcodes.ACC_PRIVATE)
                .defineMethod(METHOD_NAME, void.class, Opcodes.ACC_PUBLIC)
                .withParameters(Integer.class)  // TODO: It's the only difference with the method testPublisAndPrivate. Refactor it (DRY)
                .intercept(new SetFieldImplementation(ORIGIN_FIELD,
                        TypeDescriptionShortcuts.deepFindField(originTypeDescription, fieldName)
                                .orElseThrow(RuntimeException::new),
                        t -> t
                ))

                .make()
                .load(getClass().getClassLoader())
                .getLoaded();

        Object origin = originClass.getConstructor().newInstance();
        Object wrapper = wrapperClass.getConstructor().newInstance();
        Whitebox.setInternalState(wrapper, ORIGIN_FIELD, origin);
        Whitebox.invokeMethod(wrapper, METHOD_NAME, expectedResult);
        assertEquals(expectedResult, Whitebox.getInternalState(origin, fieldName));
    }
}
