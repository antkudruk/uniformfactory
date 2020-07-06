package com.github.antkudruk.uniformfactory.base.bytecode;

import com.github.antkudruk.uniformfactory.common.TypeDescriptionShortcuts;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodCall;
import org.junit.Ignore;
import org.junit.Test;

import java.util.function.Function;

import static junit.framework.TestCase.assertEquals;

public class InitFieldWithConstructorFieldUsingThisImplementationTest {

    public static class Wrapper {
        private final TestClass origin;

        Wrapper(TestClass origin) {
            this.origin = origin;
        }

        public TestClass getOrigin() {
            return origin;
        }
    }

    @SuppressWarnings("unused")
    public static class TestClass {
        private static Function<TestClass, Wrapper> wrapperFromOriginGenerator = Wrapper::new;
        private Wrapper wrapper;

        public Wrapper getWrapper() {
            return wrapper;
        }
    }

    @Test
    @Ignore
    public void test() throws ReflectiveOperationException {

        ByteBuddy byteBuddy = new ByteBuddy();
        Class<? extends TestClass> enhancedClass = byteBuddy
                .subclass(TestClass.class, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                .defineConstructor(Visibility.PUBLIC)
                .intercept(MethodCall.invoke(
                        TypeDescriptionShortcuts.findConstructor(Object.class).orElseThrow(RuntimeException::new))
                        .andThen(new InitFieldWithConstructorFieldUsingThisImplementation(
                                "wrapperFromOriginGenerator",
                                "wrapper"))
                )
                .make()
                .load(getClass().getClassLoader())
                .getLoaded();

        TestClass obj = enhancedClass.getConstructor().newInstance();
        assertEquals(obj, obj.getWrapper().getOrigin());
    }
}
