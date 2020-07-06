package com.github.antkudruk.uniformfactory.singleton.enhancers;

import com.github.antkudruk.uniformfactory.base.Enhancer;
import com.github.antkudruk.uniformfactory.common.TypeDescriptionShortcuts;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodCall;

class EnhancerTestUtils {

    static <W> Class<? extends W> mimicWrapperClass(
            Class<W> wrapperInterface,
            Class originClass,
            Enhancer enhancer) {
        return mimicWrapperClass(wrapperInterface, originClass, enhancer, wrapperInterface.getClassLoader());
    }

    private static <W> Class<? extends W> mimicWrapperClass(
            Class<W> wrapperInterface,
            Class originClass,
            Enhancer enhancer,
            ClassLoader classLoader) {
        DynamicType.Builder<? extends W> bbBuilder = new ByteBuddy()
                .subclass(wrapperInterface, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                .defineConstructor(Visibility.PUBLIC)
                .withParameters(originClass)
                .intercept(enhancer.addInitiation(
                        MethodCall.invoke(TypeDescriptionShortcuts
                                .findConstructor(Object.class)
                                .orElseThrow(RuntimeException::new))
                ));

        bbBuilder = enhancer.addMethod(bbBuilder);

        return bbBuilder.make()
                .load(classLoader)
                .getLoaded();
    }
}