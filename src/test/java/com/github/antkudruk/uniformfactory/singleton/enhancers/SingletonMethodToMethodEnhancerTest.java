package com.github.antkudruk.uniformfactory.singleton.enhancers;

import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialMapperImpl;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialParameterUnion;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.ConstantValue;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.ParameterValue;
import com.github.antkudruk.uniformfactory.singleton.descriptors.ResultMapperCollection;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.AnnotationParameterFilter;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.Assert.assertEquals;

public class SingletonMethodToMethodEnhancerTest {

    @SuppressWarnings("WeakerAccess")
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface Variable {

    }

    @SuppressWarnings("WeakerAccess")
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface Index {

    }

    public static class OriginImpl {
        @SuppressWarnings("WeakerAccess")
        public String getValue(@Variable String name, @Index Integer index) {
            return name + index;
        }
    }

    public interface Wrapper {
        Object getValue();
    }

    public interface WrapperWithBothParameters {
        Object getValue(String variable, Integer index);
    }

    @Test
    public void testWithoutParameters() throws ReflectiveOperationException, ClassGeneratorException {

        SingletonMethodToMethodEnhancer enhancer = new SingletonMethodToMethodEnhancer(
                "fieldAccessorFieldName",
                new TypeDescription.ForLoadedType(OriginImpl.class),
                new TypeDescription.ForLoadedType(OriginImpl.class)
                        .getDeclaredMethods()
                        .filter(ElementMatchers.named("getValue").and(ElementMatchers.takesArguments(String.class, Integer.class)))
                        .getOnly(),
                Wrapper.class.getDeclaredMethod("getValue"),
                new PartialParameterUnion.Builder()
                        .add(new PartialMapperImpl(
                                new AnnotationParameterFilter<>(Variable.class),
                                new ConstantValue<>("value ")
                        ))
                        .add(new PartialMapperImpl(
                                new AnnotationParameterFilter<>(Index.class),
                                new ConstantValue<>(1)
                        ))
                        .build(),
                new ResultMapperCollection<>(String.class)
                        .addMapper(String.class, t -> t));

        Class<? extends Wrapper> wrapperClass = EnhancerTestUtils.mimicWrapperClass(
                Wrapper.class, OriginImpl.class, enhancer);

        OriginImpl origin = new OriginImpl();
        Wrapper wrapper = wrapperClass.getConstructor(OriginImpl.class).newInstance(origin);

        assertEquals("value 1", wrapper.getValue());
    }

    @Test
    public void testWithParameterFromWrapperParameter()
            throws ReflectiveOperationException, ClassGeneratorException {

        String fieldAccessorFieldName = "fieldAccessorFieldName";
        SingletonMethodToMethodEnhancer enhancer = new SingletonMethodToMethodEnhancer(
                fieldAccessorFieldName,
                new TypeDescription.ForLoadedType(OriginImpl.class),
                new MethodDescription.ForLoadedMethod(OriginImpl.class.getDeclaredMethod("getValue", String.class, Integer.class)),
                WrapperWithBothParameters.class.getDeclaredMethod("getValue", String.class, Integer.class),
                new PartialParameterUnion.Builder()
                        .add(new PartialMapperImpl(
                                new AnnotationParameterFilter<>(Variable.class),
                                new ParameterValue<>(String.class, 0)
                        ))
                        .add(new PartialMapperImpl(
                                new AnnotationParameterFilter<>(Index.class),
                                new ParameterValue<>(Integer.class, 1)
                        ))
                        .build(),
                new ResultMapperCollection<>(String.class)
                        .addMapper(String.class, t -> t)
        );

        Class<? extends WrapperWithBothParameters> wrapperClass = EnhancerTestUtils.mimicWrapperClass(
                WrapperWithBothParameters.class, OriginImpl.class, enhancer);

        OriginImpl origin = new OriginImpl();
        WrapperWithBothParameters wrapper = wrapperClass.getConstructor(OriginImpl.class).newInstance(origin);

        assertEquals("value_1", wrapper.getValue("value_", 1));
    }
}
