package com.github.antkudruk.uniformfactory.base.bytecode;

import com.github.antkudruk.uniformfactory.common.TypeDescriptionShortcuts;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.jar.asm.Opcodes;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

public class InitMapImplementationTest {

    private static final String FIELD_NAME_0 = "field0";
    private static final String FIELD_NAME_1 = "field1";

    public static class OriginImpl {

    }

    public static class IntermediateWrapper0 {
        private final OriginImpl origin;

        public IntermediateWrapper0(OriginImpl origin) {
            this.origin = origin;
        }

        public OriginImpl getOrigin() {
            return origin;
        }
    }

    public static class IntermediateWrapper1 {
        private final OriginImpl origin;

        public IntermediateWrapper1(OriginImpl origin) {
            this.origin = origin;
        }

        public OriginImpl getOrigin() {
            return origin;
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test() throws ReflectiveOperationException {
        ByteBuddy byteBuddy = new ByteBuddy();

        Map<String, TypeDescription> types = new HashMap<>();
        types.put("alpha", new TypeDescription.ForLoadedType(IntermediateWrapper0.class));
        types.put("beta", new TypeDescription.ForLoadedType(IntermediateWrapper1.class));

        Class wrapperClass = byteBuddy
                .subclass(Object.class, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                .defineField(FIELD_NAME_0, Map.class, Opcodes.ACC_PRIVATE)
                .defineConstructor(Visibility.PUBLIC)
                .withParameters(OriginImpl.class)
                .intercept(MethodCall
                        .invoke(TypeDescriptionShortcuts
                                .findConstructor(Object.class)
                                .orElseThrow(RuntimeException::new))
                        .andThen(new InitMapImplementation(
                                FIELD_NAME_0,
                                new TypeDescription.ForLoadedType(OriginImpl.class),
                                types
                        ))
                )
                .make()
                .load(getClass().getClassLoader())
                .getLoaded();

        OriginImpl origin = new OriginImpl();
        Object wrapper = wrapperClass.getConstructor(OriginImpl.class).newInstance(origin);
        Map<String, Object> map = Whitebox.getInternalState(wrapper, FIELD_NAME_0);

        assertEquals(IntermediateWrapper0.class, map.get("alpha").getClass());
        assertEquals(IntermediateWrapper1.class, map.get("beta").getClass());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTwoMethods() throws ReflectiveOperationException {
        ByteBuddy byteBuddy = new ByteBuddy();

        Map<String, TypeDescription> types = new HashMap<>();
        types.put("alpha", new TypeDescription.ForLoadedType(IntermediateWrapper0.class));
        types.put("beta", new TypeDescription.ForLoadedType(IntermediateWrapper1.class));

        Class wrapperClass = byteBuddy
                .subclass(Object.class, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                .defineField(FIELD_NAME_0, Map.class, Opcodes.ACC_PRIVATE)
                .defineField(FIELD_NAME_1, Map.class, Opcodes.ACC_PRIVATE)
                .defineConstructor(Visibility.PUBLIC)
                .withParameters(OriginImpl.class)
                .intercept(MethodCall
                        .invoke(TypeDescriptionShortcuts
                                .findConstructor(Object.class)
                                .orElseThrow(RuntimeException::new))
                        .andThen(new InitMapImplementation(
                                FIELD_NAME_0,
                                new TypeDescription.ForLoadedType(OriginImpl.class),
                                types
                        ))
                        .andThen(new InitMapImplementation(
                                FIELD_NAME_1,
                                new TypeDescription.ForLoadedType(OriginImpl.class),
                                types
                        ))
                )
                .make()
                .load(getClass().getClassLoader())
                .getLoaded();

        OriginImpl origin = new OriginImpl();
        Object wrapper = wrapperClass.getConstructor(OriginImpl.class).newInstance(origin);

        Map<String, Object> map0 = Whitebox.getInternalState(wrapper, FIELD_NAME_0);
        Map<String, Object> map1 = Whitebox.getInternalState(wrapper, FIELD_NAME_1);

        assertEquals(IntermediateWrapper0.class, map0.get("alpha").getClass());
        assertEquals(IntermediateWrapper1.class, map0.get("beta").getClass());

        assertEquals(IntermediateWrapper0.class, map1.get("alpha").getClass());
        assertEquals(IntermediateWrapper1.class, map1.get("beta").getClass());
    }
}
