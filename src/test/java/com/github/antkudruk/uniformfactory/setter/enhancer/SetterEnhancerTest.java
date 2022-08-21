package com.github.antkudruk.uniformfactory.setter.enhancer;

import com.github.antkudruk.uniformfactory.setter.enhanncers.SetterEnhancer;
import com.github.antkudruk.uniformfactory.singleton.argument.exceptions.ParameterTranslatorNotFound;
import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.AnyParameterFilter;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialMapperImpl;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialParameterUnion;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.ParameterValue;
import com.github.antkudruk.uniformfactory.singleton.enhancers.EnhancerTestUtils;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import static junit.framework.TestCase.assertEquals;


public class SetterEnhancerTest {

    private static String FIELD_VALUE = "Field value";

    public static class OriginImpl {
        @SuppressWarnings("unused")
        public String field = FIELD_VALUE;
    }

    public interface Wrapper {
        void setValue(Object argument);
    }

    @Test
    public void test() throws ReflectiveOperationException, ParameterTranslatorNotFound {

        String fieldAccessorFieldName = "fieldAccessorFieldName";
        SetterEnhancer enhancer = new SetterEnhancer(
                fieldAccessorFieldName,
                new TypeDescription.ForLoadedType(OriginImpl.class),
                new TypeDescription.ForLoadedType(OriginImpl.class)
                        .getDeclaredFields().filter(ElementMatchers.named("field")).getOnly(),
                Wrapper.class.getDeclaredMethod("setValue", Object.class),
                new PartialParameterUnion.Builder()
                        .add(
                                new PartialMapperImpl(
                                        new AnyParameterFilter(),
                                        new ParameterValue<>(String.class, 0)
                                )
                        )
                        .build()
        );

        Class<? extends Wrapper> wrapperClass = EnhancerTestUtils.mimicWrapperClass(Wrapper.class, OriginImpl.class, enhancer);

        OriginImpl origin = new OriginImpl();
        Wrapper wrapper = wrapperClass.getConstructor(OriginImpl.class).newInstance(origin);

        wrapper.setValue("test");

        Object state = Whitebox.getInternalState(origin, "field");

        assertEquals("test", state);
    }
}
