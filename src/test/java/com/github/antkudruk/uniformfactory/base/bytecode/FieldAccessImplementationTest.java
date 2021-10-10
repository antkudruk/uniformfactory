package com.github.antkudruk.uniformfactory.base.bytecode;

import com.github.antkudruk.uniformfactory.common.TypeDescriptionShortcuts;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import static junit.framework.TestCase.assertEquals;

public class FieldAccessImplementationTest {

    private static final String ORIGIN_FIELD = "originField";
    private static final String METHOD_NAME = "getMethod";

    public static class OriginImpl {
        @SuppressWarnings("unused")
        public String publicField = "10";
        @SuppressWarnings("unused")
        private String privateField = "20";
    }

    @SuppressWarnings("WeakerAccess")
    public static class OriginDerived extends OriginImpl {
    }

    @Test
    public void testPublic() throws Exception {
        test(OriginImpl.class, "publicField", "10");
    }

    @Test
    public void testPrivate() throws Exception {
        test(OriginImpl.class, "privateField", "20");
    }

    @Test
    public void testSuperPublic() throws Exception {
        test(OriginDerived.class, "publicField", "10");
    }

    @Test
    public void testSuperPrivate() throws Exception {
        test(OriginDerived.class, "privateField", "20");
    }

    @SuppressWarnings("unchecked")
    private void test(Class originClass, String fieldName, Object expectedResult) throws Exception {

        TypeDescription originTypeDescription
                = new TypeDescription.ForLoadedType(originClass);

        ByteBuddy byteBuddy = new ByteBuddy();

        Class<?> wrapperClass = byteBuddy
                .subclass(Object.class)
                .defineField(ORIGIN_FIELD, originClass, Opcodes.ACC_PRIVATE)
                .defineMethod(METHOD_NAME, Object.class, Opcodes.ACC_PUBLIC)
                .intercept(new FieldAccessImplementation(ORIGIN_FIELD,
                        TypeDescriptionShortcuts.deepFindField(originTypeDescription, fieldName)
                                .orElseThrow(RuntimeException::new),
                        t -> t
                ))

                .make()
                .load(getClass().getClassLoader())
                .getLoaded();

        Object wrapper = wrapperClass.getConstructor().newInstance();
        Whitebox.setInternalState(wrapper, ORIGIN_FIELD, originClass.getConstructor().newInstance());
        assertEquals(expectedResult, Whitebox.invokeMethod(wrapper, METHOD_NAME));
    }

    @Test
    public void testPublicTransformingValue() throws Exception {

        TypeDescription originTypeDescription
                = new TypeDescription.ForLoadedType(OriginImpl.class);

        ByteBuddy byteBuddy = new ByteBuddy();

        Class<?> wrapperClass = byteBuddy
                .subclass(Object.class)
                .defineField(ORIGIN_FIELD, OriginImpl.class, Opcodes.ACC_PRIVATE)
                .defineMethod(METHOD_NAME, Object.class, Opcodes.ACC_PUBLIC)
                .intercept(new FieldAccessImplementation(ORIGIN_FIELD,
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
        Whitebox.setInternalState(wrapper, ORIGIN_FIELD, new OriginImpl());
        assertEquals("1010", Whitebox.invokeMethod(wrapper, METHOD_NAME));
    }
}
