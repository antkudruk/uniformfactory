package com.github.antkudruk.uniformfactory.setter.descriptors;

import com.github.antkudruk.uniformfactory.base.Enhancer;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.AnyParameterFilter;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialMapperImpl;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.ParameterValue;
import com.github.antkudruk.uniformfactory.singleton.enhancers.EnhancerTestUtils;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static junit.framework.TestCase.assertEquals;

public class SetterDescriptorTest {

    public interface Wrapper {
        void set(String s);
    }

    public static class HasField {
        @Marker
        private String name;
    }

    public static class WithoutField {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Marker {

    }

    @Test
    public void whenFieldExists_thenReturnSetFieldEnhancer() throws ReflectiveOperationException, ClassGeneratorException {
        // given
        SetterDescriptor setterDescriptor = new SetterDescriptor.Builder(
                Wrapper.class.getMethod("set", String.class),
                String.class
        )
        .setAnnotation(Marker.class)
        .addParameterTranslator(
                new PartialMapperImpl(
                        new AnyParameterFilter(),
                        new ParameterValue<>(String.class, 0)
                )
        )
        .build();

        // when
        Enhancer enhancer = setterDescriptor.getEnhancer(new TypeDescription.ForLoadedType(HasField.class));

        // then
        Class<? extends Wrapper> wrapperClass = EnhancerTestUtils.mimicWrapperClass(
                Wrapper.class, HasField.class, enhancer);
        HasField hasField = new HasField();
        Wrapper wrapper = wrapperClass.getConstructor(HasField.class).newInstance(hasField);
        wrapper.set("test");
        assertEquals("test", hasField.name);
    }

    @Test
    public void whenFieldAbsent_thenReturnDoNothingEnhancer() throws ReflectiveOperationException, ClassGeneratorException {
        // given
        SetterDescriptor setterDescriptor = new SetterDescriptor.Builder(
                Wrapper.class.getMethod("set", String.class), String.class)
                .setAnnotation(Marker.class)
                .build();

        // when
        Enhancer enhancer = setterDescriptor.getEnhancer(new TypeDescription.ForLoadedType(WithoutField.class));

        // then
        Class<? extends Wrapper> wrapperClass = EnhancerTestUtils.mimicWrapperClass(
                Wrapper.class, WithoutField.class, enhancer);
        WithoutField hasField = new WithoutField();
        Wrapper wrapper = wrapperClass.getConstructor(WithoutField.class).newInstance(hasField);
        wrapper.set("test");
    }
}
