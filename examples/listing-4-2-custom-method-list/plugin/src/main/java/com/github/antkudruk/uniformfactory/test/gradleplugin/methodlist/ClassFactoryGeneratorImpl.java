package com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.pluginbuilder.MetaClassFactory;
import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.AnyParameterFilter;
import lombok.SneakyThrows;

import java.util.function.Function;

public class ClassFactoryGeneratorImpl implements MetaClassFactory<Wrapper> {

    private final ClassFactory<Wrapper> classFactory;

    public ClassFactoryGeneratorImpl() throws NoSuchMethodException {
        this.classFactory = new ClassFactory.Builder<>(Wrapper.class)
                .addMethodList(Wrapper.class.getMethod("getProcessors"), Processor.class)

                .defaultElementSource()

                .setMarkerAnnotation(Processor.Process.class)

                // Setting up element factory for the list - getters
                .getterElementFactory(Boolean.class)

                // Describes how to map results from origin methods to the adapter methd result
                .addResultTranslator(void.class, t -> true)
                .addResultTranslator(Long.class, t -> t >= 0)
                .addResultTranslator(String.class, "yes"::equalsIgnoreCase)
                .addResultTranslator(Boolean.class, t -> t)

                // Describes how to map parameters from the adapter method to origin methods
                .parameterSource(String.class, 0)
                .applyTo(new AnyParameterFilter())
                .addExtends(Integer.class, Integer::parseInt)
                .finishParameterDescription()

                .finishElementFactory()
                .endElementSource()
                .endMethodDescription()

                .addMethodList(Wrapper.class.getMethod("getDescriptors"), CssPropertySetter.class)
                .defaultElementSource()
                .setMarkerAnnotation(CssProperty.class)
                .setterElementFactory()
                .parameterSource(String.class, 0)
                .applyTo(new AnyParameterFilter())
                .addExtends(Integer.class, Integer::parseInt)
                .addExtends(Long.class, Long::parseLong)
                .addExtends(Boolean.class, Boolean::parseBoolean)
                .finishParameterDescription()

                .finishElementFactory()
                .endElementSource()
                .endMethodDescription()

                .build();
    }

    @Override
    @SneakyThrows(ClassGeneratorException.class)
    public <O> Function<O, ? extends Wrapper> generateMetaClass(Class<O> originClass) {
        return classFactory.buildWrapperFactory(originClass);
    }
}
