package com.github.antkudruk.uniformfactory.methodcollection;

import com.github.antkudruk.uniformfactory.methodmap.descriptors.MethodMapDescriptor;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.SuperMethodCall;
import org.junit.Test;

import java.lang.reflect.Method;

import static junit.framework.TestCase.assertEquals;

public class ElementGeneratorTest {

    public static class OriginImpl {

    }

    public interface FunctionalInterface {
        String concat(String a, String b);
    }

    public static class IntermediateFieldBasedClass {
        private final OriginImpl origin;

        public IntermediateFieldBasedClass(OriginImpl origin) {
            this.origin = origin;
        }

        public OriginImpl getOrigin() {
            return origin;
        }

        @SuppressWarnings("WeakerAccess")
        public String concat() {
            return " ";
        }
    }

    public static class IntermediateMethodBasedClass {
        private final OriginImpl origin;

        public IntermediateMethodBasedClass(OriginImpl origin) {
            this.origin = origin;
        }

        public OriginImpl getOrigin() {
            return origin;
        }

        @SuppressWarnings("WeakerAccess")
        public String concat(String a, String b) {
            return a + " " + b;
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMethodBased() throws ReflectiveOperationException {

        DynamicType.Unloaded unloadedIntermediateClass = createUnloaded(
                IntermediateMethodBasedClass.class,
                IntermediateMethodBasedClass.class.getMethod("concat", String.class, String.class));

        Class cls = ElementGenerator.INSTANCE.generate(
                new TypeDescription.ForLoadedType(OriginImpl.class),
                new TypeDescription.ForLoadedType(FunctionalInterface.class),
                unloadedIntermediateClass,
                MethodCall::withAllArguments,
                MethodMapDescriptor.INTERMEDIATE_WRAPPER_FIELD_NAME
        ).load(getClass().getClassLoader()).getLoaded();
        OriginImpl origin = new OriginImpl();
        Object obj = cls.getConstructor(OriginImpl.class).newInstance(origin);
        FunctionalInterface fn = (FunctionalInterface) obj;
        assertEquals("Foo Bar", fn.concat("Foo", "Bar"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFieldBased() throws ReflectiveOperationException {

        DynamicType.Unloaded unloadedIntermediateClass = createUnloaded(
                IntermediateFieldBasedClass.class,
                IntermediateFieldBasedClass.class.getMethod("concat"));

        Class cls = ElementGenerator.INSTANCE.generate(
                new TypeDescription.ForLoadedType(OriginImpl.class),
                new TypeDescription.ForLoadedType(FunctionalInterface.class),
                unloadedIntermediateClass,
                m -> m,
                MethodMapDescriptor.INTERMEDIATE_WRAPPER_FIELD_NAME
        ).load(getClass().getClassLoader()).getLoaded();
        OriginImpl origin = new OriginImpl();
        Object obj = cls.getConstructor(OriginImpl.class).newInstance(origin);
        FunctionalInterface fn = (FunctionalInterface) obj;
        assertEquals(" ", fn.concat("Foo", "Bar"));
    }

    private DynamicType.Unloaded createUnloaded(Class<?> baseClass, Method method) throws NoSuchMethodException {
        return new ByteBuddy()
                .subclass(baseClass, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                .defineConstructor(Visibility.PUBLIC)
                .withParameters(OriginImpl.class)
                .intercept(MethodCall.invoke(baseClass.getConstructor(OriginImpl.class))
                        .withAllArguments())
                .define(method)
                .intercept(SuperMethodCall.INSTANCE)
                .make();
    }
}
