package com.github.antkudruk.uniformfactory.singleton.enhancers;

import com.github.antkudruk.uniformfactory.singleton.descriptors.ResultMapperCollection;
import com.github.antkudruk.uniformfactory.singleton.descriptors.WrapperMethodTypesException;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class SingletonMethodToFieldEnhancerTest {

    private static String FIELD_VALUE = "Field value";

    public static class OriginImpl {
        @SuppressWarnings("unused")
        public static String field = FIELD_VALUE;
    }

    public interface Wrapper {
        Object getValue();
    }

    @Test
    public void test() throws ReflectiveOperationException, WrapperMethodTypesException {

        String fieldAccessorFieldName = "fieldAccessorFieldName";
        SingletonMethodToFieldEnhancer enhancer = new SingletonMethodToFieldEnhancer(
                fieldAccessorFieldName,
                new TypeDescription.ForLoadedType(OriginImpl.class),
                new TypeDescription.ForLoadedType(OriginImpl.class)
                        .getDeclaredFields().filter(ElementMatchers.named("field")).getOnly(),
                Wrapper.class.getDeclaredMethod("getValue"),
                new ResultMapperCollection<>(String.class)
                        .addMapper(String.class, t -> t));

        Class<? extends Wrapper> wrapperClass = EnhancerTestUtils.mimicWrapperClass(Wrapper.class, OriginImpl.class, enhancer);

        OriginImpl origin = new OriginImpl();
        Wrapper wrapper = wrapperClass.getConstructor(OriginImpl.class).newInstance(origin);

        assertEquals(FIELD_VALUE, wrapper.getValue());
    }
}
