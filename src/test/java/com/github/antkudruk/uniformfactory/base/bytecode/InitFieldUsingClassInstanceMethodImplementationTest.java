package com.github.antkudruk.uniformfactory.base.bytecode;

import com.github.antkudruk.uniformfactory.pluginbuilder.MetaClassFactory;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.jar.asm.Opcodes;
import org.junit.Test;

import java.util.function.Function;

import static junit.framework.TestCase.assertEquals;

public class InitFieldUsingClassInstanceMethodImplementationTest {

    private static final Function<TestClass, Wrapper> ORIGIN_TO_WRAPPER_GENERATOR = Wrapper::new;

    public static class GeneratorSingletonHolder {
        @SuppressWarnings("unused")
        public static final Meta INSTANCE = new Meta();
    }

    public static class Meta implements MetaClassFactory<Wrapper> {
        @SuppressWarnings("unchecked")
        @Override
        public <O> Function<O, ? extends Wrapper> generateMetaClass(Class<O> originClass) {
            return (Function<O, ? extends Wrapper>) ORIGIN_TO_WRAPPER_GENERATOR;
        }
    }

    public static class Wrapper {
        private final TestClass origin;

        Wrapper(TestClass origin) {
            this.origin = origin;
        }

        public TestClass getOrigin() {
            return origin;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class TestClass {
        public static Function<TestClass, Wrapper> wrapperFromOriginGenerator = Wrapper::new;
    }

    @Test
    public void test() throws ReflectiveOperationException {

        ByteBuddy byteBuddy = new ByteBuddy();
        Class<? extends TestClass> enhancedClass = byteBuddy
                .subclass(TestClass.class, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                .defineMethod("init", void.class, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
                .intercept(new InitFieldUsingClassInstanceMethodImplementation(
                        new TypeDescription.ForLoadedType(GeneratorSingletonHolder.class),
                        "wrapperFromOriginGenerator",
                        Meta.class))
                .make()
                .load(getClass().getClassLoader())
                .getLoaded();

        enhancedClass.getDeclaredMethod("init").invoke(null);

        assertEquals(ORIGIN_TO_WRAPPER_GENERATOR, TestClass.wrapperFromOriginGenerator);
    }
}
