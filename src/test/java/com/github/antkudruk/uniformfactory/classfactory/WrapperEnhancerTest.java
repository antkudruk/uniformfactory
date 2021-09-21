package com.github.antkudruk.uniformfactory.classfactory;

import com.github.antkudruk.uniformfactory.base.bytecode.EmptyImplementation;
import com.github.antkudruk.uniformfactory.pluginbuilder.MetaClassFactory;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class WrapperEnhancerTest {

    @SuppressWarnings("WeakerAccess")
    public static final WrapperEnhancerTest.Adapter ADAPTER = new Adapter();

    @SuppressWarnings("WeakerAccess")
    public static class Adapter {

    }

    public interface Origin {
        Adapter getAdapter();
    }

    public static class MetaClassFactoryGenerator implements MetaClassFactory<Adapter> {

        @SuppressWarnings("unused")
        public static MetaClassFactoryGenerator INSTANCE = new MetaClassFactoryGenerator();

        @Override
        public <O> Function<O, ? extends Adapter> generateMetaClass(Class<O> originClass) {
            return o -> ADAPTER;
        }
    }

    @Test
    public void generateClassWithWrapper () throws ReflectiveOperationException {
        // given
        WrapperEnhancer wrapperEnhancer = new WrapperEnhancer(
                Origin.class,
                MetaClassFactoryGenerator.class,
                new TypeDescription.ForLoadedType(MetaClassFactoryGenerator.class),
                "classFactoryGeneratorFieldName",
                Adapter.class,
                "wrapperFieldName",
                "getAdapter"

        );

        // when
        DynamicType.Builder<Origin> dynamicTypeBuilder = new ByteBuddy()
                .subclass(Origin.class, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                .invokable(ElementMatchers.isTypeInitializer())
                .intercept(wrapperEnhancer.addStaticInitiation(new EmptyImplementation()))
                .defineConstructor(Visibility.PUBLIC)
                .intercept(wrapperEnhancer.addInitiation(SuperMethodCall.INSTANCE));

        dynamicTypeBuilder = wrapperEnhancer.addMethod(dynamicTypeBuilder);

        Class<? extends Origin> generatedClass = dynamicTypeBuilder
                .make()
                .load(getClass().getClassLoader())
                .getLoaded();

        // then
        Origin origin = generatedClass.getConstructor().newInstance();
        Adapter adapter = origin.getAdapter();
        assertEquals(ADAPTER, adapter);
    }
}
