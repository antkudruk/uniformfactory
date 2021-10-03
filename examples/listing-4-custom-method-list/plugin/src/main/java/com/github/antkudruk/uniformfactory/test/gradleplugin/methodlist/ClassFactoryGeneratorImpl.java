package com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.pluginbuilder.DefaultMetaClassFactory;
import com.github.antkudruk.uniformfactory.pluginbuilder.MetaClassFactory;
import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.AnyParameterFilter;
import net.bytebuddy.description.type.TypeDescription;

import java.lang.reflect.Constructor;
import java.util.function.Function;

public class ClassFactoryGeneratorImpl implements MetaClassFactory<Wrapper> {

    private final ClassFactory<Wrapper> classFactory;

    public ClassFactoryGeneratorImpl() throws NoSuchMethodException {
        this.classFactory = new ClassFactory.ShortcutBuilder<>(Wrapper.class)
                .addMethodList(
                        Wrapper.class.getMethod("getProcessors"),
                        boolean.class
                )
                .setMarkerAnnotation(Processor.Process.class)
                .setFunctionalInterface(Processor.class)

                .addResultTranslator(void.class, t -> true)
                .addResultTranslator(Long.class, t -> t >= 0)
                .addResultTranslator(String.class, "yes"::equalsIgnoreCase)
                .addResultTranslator(Boolean.class, t -> t)

                .parameterSource(String.class, 0)
                .applyTo(new AnyParameterFilter())
                .addTranslator(Integer.class, Integer::parseInt)
                .finishParameterDescription()

                .endMethodDescription()

                .build();
    }

    @Override
    public <O> Function<O, ? extends Wrapper> generateMetaClass(Class<O> originClass) {
        try {
            Constructor<? extends Wrapper> wrapperConstructor = classFactory
                    .build(new TypeDescription.ForLoadedType(originClass))
                    .load(DefaultMetaClassFactory.class.getClassLoader())
                    .getLoaded()
                    .getConstructor(originClass);

            return new WrapperObjectGenerator<>(wrapperConstructor);
        } catch (ClassGeneratorException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static class WrapperObjectGenerator<O> extends DefaultMetaClassFactory.WrapperObjectGenerator<O, Wrapper> {

        WrapperObjectGenerator(Constructor<? extends Wrapper> wrapperConstructor) {
            super(wrapperConstructor);
        }

        @Override
        public Wrapper apply(O t) {
            Wrapper w = super.apply(t);
            CallableObjectsRegistry.INSTANCE.addObject(w);
            return w;
        }
    }
}
