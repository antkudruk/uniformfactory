package com.github.antkudruk.uniformfactory.classfactory;

import com.github.antkudruk.uniformfactory.base.Enhancer;
import com.github.antkudruk.uniformfactory.base.bytecode.InitFieldUsingClassInstanceMethodImplementation;
import com.github.antkudruk.uniformfactory.base.bytecode.InitFieldWithConstructorFieldUsingThisImplementation;
import com.github.antkudruk.uniformfactory.pluginbuilder.MetaClassFactory;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.function.Function;

/**
 *
 * Generates a class by the following rule:
 *
 * <pre>
 *     {@code
 *     class Origin {
 *         static Function<Origin, Adapter> classFactoryGenerator;
 *         Adapter adapter;
 *
 *         static {
 *             classFactoryGenerator = SingletonHolder.INSTANCE.generateMetaClass(this.getClass());
 *             // ...
 *         }
 *
 *         public Origin() {
 *             this.adapter = classFactory.apply();
 *             // ...
 *         }
 *
 *         public Adapter getAdapter() {
 *             return adapter
 *         }
 *
 *         // ...
 *     }
 *     }
 * </pre>
 *
 * <ul>
 *     <li>Class Factory Generator</li>
 *     <li>Adapter object</li>
 * </ul>
 */
public class WrapperEnhancer implements Enhancer {

    private final Class originInterface;
    private final Class<? extends MetaClassFactory<?>> metaClassFactoryGenerator;
    private final TypeDescription classGeneratorSingleton;
    private final String classFactoryGeneratorFieldName;
    private final Class<?> wrapperClass;
    private final String wrapperFieldName;
    private final String getWrapperMethodName;

    public WrapperEnhancer(
            Class originInterface,
            Class<? extends MetaClassFactory<?>> metaClassFactoryGenerator,

            TypeDescription classGeneratorSingleton,
            String classFactoryGeneratorFieldName,

            Class<?> wrapperClass,
            String wrapperFieldName,
            String getWrapperMethodName) {
        this.originInterface = originInterface;
        this.metaClassFactoryGenerator = metaClassFactoryGenerator;
        this.classGeneratorSingleton = classGeneratorSingleton;
        this.classFactoryGeneratorFieldName = classFactoryGeneratorFieldName;
        this.wrapperClass = wrapperClass;
        this.wrapperFieldName = wrapperFieldName;
        this.getWrapperMethodName = getWrapperMethodName;
    }

    @Override
    public Implementation.Composable addStaticInitiation(Implementation.Composable existingImplementation) {
        return existingImplementation.andThen(new InitFieldUsingClassInstanceMethodImplementation(
                classGeneratorSingleton,
                classFactoryGeneratorFieldName,
                metaClassFactoryGenerator
        ));
    }

    @Override
    public Implementation.Composable addInitiation(Implementation.Composable existingImplementation) {
        return existingImplementation.andThen(
                new InitFieldWithConstructorFieldUsingThisImplementation(
                        classFactoryGeneratorFieldName, wrapperFieldName)
        );
    }

    @Override
    public <W> DynamicType.Builder<W> addMethod(DynamicType.Builder<W> bbBuilder) {
        return bbBuilder
                .defineField(wrapperFieldName, wrapperClass,
                        Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC)
                .defineField(classFactoryGeneratorFieldName, Function.class,
                        Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC)
                .define(new TypeDescription.ForLoadedType(originInterface)
                        .getDeclaredMethods()
                        .filter(ElementMatchers.named(getWrapperMethodName))
                        .getOnly())
                .intercept(FieldAccessor.ofField(wrapperFieldName));
    }
}
