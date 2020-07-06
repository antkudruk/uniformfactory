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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class InitListImplementationTest {

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

        List<TypeDescription> types = Arrays.asList(
                new TypeDescription.ForLoadedType(IntermediateWrapper0.class),
                new TypeDescription.ForLoadedType(IntermediateWrapper1.class)
        );

        Class wrapperClass = byteBuddy
                .subclass(Object.class, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                .defineField(FIELD_NAME_0, List.class, Opcodes.ACC_PRIVATE)
                .defineConstructor(Visibility.PUBLIC)
                .withParameters(OriginImpl.class)
                .intercept(MethodCall
                        .invoke(TypeDescriptionShortcuts
                                .findConstructor(Object.class)
                                .orElseThrow(RuntimeException::new))
                        .andThen(new InitListImplementation(
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
        List<Object> list = Whitebox.getInternalState(wrapper, FIELD_NAME_0);

        assertEquals(IntermediateWrapper0.class, list.get(0).getClass());
        assertEquals(IntermediateWrapper1.class, list.get(1).getClass());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTwoMethods() throws ReflectiveOperationException {
        ByteBuddy byteBuddy = new ByteBuddy();

        List<TypeDescription> types0 = Arrays.asList(
                new TypeDescription.ForLoadedType(IntermediateWrapper0.class),
                new TypeDescription.ForLoadedType(IntermediateWrapper1.class));

        List<TypeDescription> types1 = Collections.singletonList(
                new TypeDescription.ForLoadedType(IntermediateWrapper0.class));

        Class wrapperClass = byteBuddy
                .subclass(Object.class, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                .defineField(FIELD_NAME_0, List.class, Opcodes.ACC_PRIVATE)
                .defineField(FIELD_NAME_1, List.class, Opcodes.ACC_PRIVATE)
                .defineConstructor(Visibility.PUBLIC)
                .withParameters(OriginImpl.class)
                .intercept(MethodCall
                        .invoke(TypeDescriptionShortcuts
                                .findConstructor(Object.class)
                                .orElseThrow(RuntimeException::new))
                        .andThen(new InitListImplementation(
                                FIELD_NAME_0,
                                new TypeDescription.ForLoadedType(OriginImpl.class),
                                types0
                        ))
                        .andThen(new InitListImplementation(
                                FIELD_NAME_1,
                                new TypeDescription.ForLoadedType(OriginImpl.class),
                                types1
                        ))
                )
                .make()
                .load(getClass().getClassLoader())
                .getLoaded();

        OriginImpl origin = new OriginImpl();
        Object wrapper = wrapperClass.getConstructor(OriginImpl.class).newInstance(origin);
        List<Object> list0 = Whitebox.getInternalState(wrapper, FIELD_NAME_0);
        assertEquals(IntermediateWrapper0.class, list0.get(0).getClass());
        assertEquals(IntermediateWrapper1.class, list0.get(1).getClass());
        assertEquals(2, list0.size());

        List<Object> list1 = Whitebox.getInternalState(wrapper, FIELD_NAME_1);
        assertEquals(IntermediateWrapper0.class, list1.get(0).getClass());
        assertEquals(1, list1.size());
    }
}
