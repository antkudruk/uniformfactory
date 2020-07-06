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

import static junit.framework.TestCase.assertEquals;

public class InitInnerFieldWithArgumentImplementationTest {

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
        Class wrapperClass = byteBuddy
                .subclass(Object.class, ConstructorStrategy.Default.NO_CONSTRUCTORS)

                .defineField(FIELD_NAME_0, IntermediateWrapper0.class, Opcodes.ACC_PRIVATE)

                .defineConstructor(Visibility.PUBLIC)
                .withParameters(OriginImpl.class)
                .intercept(MethodCall.invoke(
                        TypeDescriptionShortcuts.findConstructor(Object.class).orElseThrow(RuntimeException::new))
                        .andThen(new InitInnerFieldWithArgumentImplementation(
                                FIELD_NAME_0,
                                new TypeDescription.ForLoadedType(OriginImpl.class),
                                new TypeDescription.ForLoadedType(IntermediateWrapper0.class)
                        ))
                )

                .make()
                .load(getClass().getClassLoader())
                .getLoaded();

        OriginImpl origin = new OriginImpl();
        Object wrapper = wrapperClass.getConstructor(OriginImpl.class).newInstance(origin);

        IntermediateWrapper0 intermediateWrapper0 = Whitebox.getInternalState(wrapper, FIELD_NAME_0);
        assertEquals(origin, intermediateWrapper0.getOrigin());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTwo() throws ReflectiveOperationException {
        ByteBuddy byteBuddy = new ByteBuddy();
        Class wrapperClass = byteBuddy
                .subclass(Object.class, ConstructorStrategy.Default.NO_CONSTRUCTORS)

                .defineField(FIELD_NAME_0, IntermediateWrapper0.class, Opcodes.ACC_PRIVATE)
                .defineField(FIELD_NAME_1, IntermediateWrapper1.class, Opcodes.ACC_PRIVATE)

                .defineConstructor(Visibility.PUBLIC)
                .withParameters(OriginImpl.class)
                .intercept(MethodCall.invoke(
                        TypeDescriptionShortcuts.findConstructor(Object.class).orElseThrow(RuntimeException::new))
                        .andThen(new InitInnerFieldWithArgumentImplementation(
                                FIELD_NAME_0,
                                new TypeDescription.ForLoadedType(OriginImpl.class),
                                new TypeDescription.ForLoadedType(IntermediateWrapper0.class)
                        ))
                        .andThen(new InitInnerFieldWithArgumentImplementation(
                                FIELD_NAME_1,
                                new TypeDescription.ForLoadedType(OriginImpl.class),
                                new TypeDescription.ForLoadedType(IntermediateWrapper1.class)
                        ))
                )

                .make()
                .load(getClass().getClassLoader())
                .getLoaded();

        OriginImpl origin = new OriginImpl();
        Object wrapper = wrapperClass.getConstructor(OriginImpl.class).newInstance(origin);

        IntermediateWrapper0 intermediateWrapper0 = Whitebox.getInternalState(wrapper, FIELD_NAME_0);
        IntermediateWrapper1 intermediateWrapper1 = Whitebox.getInternalState(wrapper, FIELD_NAME_1);

        assertEquals(origin, intermediateWrapper0.getOrigin());
        assertEquals(origin, intermediateWrapper1.getOrigin());
    }
}
