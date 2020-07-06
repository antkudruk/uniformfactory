package com.github.antkudruk.uniformfactory.singleton.enhancers;

import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;

public class SingletonMethodToConstantEnhancerTest {

    public static class OriginImpl { }

    public interface Wrapper {
        Object getValue();
    }

    @Test
    public void testNotNull() throws ReflectiveOperationException  {

        String constantValue = "Constant value";

        String constantReturnerFieldName = "constantReturnerFieldName";

        SingletonMethodToConstantEnhancer<String> enhancer = new SingletonMethodToConstantEnhancer<>(
                constantReturnerFieldName,
                new TypeDescription.ForLoadedType(OriginImpl.class),
                constantValue,
                Wrapper.class.getDeclaredMethod("getValue"));

        Class<? extends Wrapper> wrapperClass = EnhancerTestUtils.mimicWrapperClass(
                Wrapper.class,
                OriginImpl.class,
                enhancer);

        OriginImpl origin = new OriginImpl();
        Wrapper wrapper = wrapperClass.getConstructor(OriginImpl.class).newInstance(origin);

        assertEquals(constantValue, wrapper.getValue());
    }

    @Test
    public void testNull() throws ReflectiveOperationException  {

        String constantReturnerFieldName = "constantReturnerFieldName";

        SingletonMethodToConstantEnhancer<String> enhancer = new SingletonMethodToConstantEnhancer<>(
                constantReturnerFieldName,
                new TypeDescription.ForLoadedType(OriginImpl.class),
                null,
                Wrapper.class.getDeclaredMethod("getValue"));

        Class<? extends Wrapper> wrapperClass = EnhancerTestUtils.mimicWrapperClass(
                Wrapper.class,
                OriginImpl.class,
                enhancer);

        OriginImpl origin = new OriginImpl();
        Wrapper wrapper = wrapperClass.getConstructor(OriginImpl.class).newInstance(origin);

        assertNull(wrapper.getValue());
    }
}
