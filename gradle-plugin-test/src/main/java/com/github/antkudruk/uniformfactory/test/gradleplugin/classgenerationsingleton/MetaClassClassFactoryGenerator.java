package com.github.antkudruk.uniformfactory.test.gradleplugin.classgenerationsingleton;

import com.github.antkudruk.uniformfactory.test.gradleplugin.classgenerationsingleton.origin.Origin;
import com.github.antkudruk.uniformfactory.test.gradleplugin.classgenerationsingleton.wrapper.Wrapper;
import com.github.antkudruk.uniformfactory.test.gradleplugin.classgenerationsingleton.wrapper.WrapperImpl;
import com.github.antkudruk.uniformfactory.pluginbuilder.MetaClassFactory;
import com.github.antkudruk.uniformfactory.pluginbuilder.WrapperPlugin;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class MetaClassClassFactoryGenerator {

    public static WrapperPlugin wrapperPlugin() {
        return new WrapperPlugin.Builder<>(Wrapper.class)
                .setOriginInterface(Origin.class)
                .setGetWrapperMethodName("getWrapper")
                .setTypeMarker(Origin.Marker.class)
                .setClassFactoryGenerator(Meta.class)
                .build();
    }

    public static class Meta implements MetaClassFactory<Wrapper> {

        private final AtomicInteger classIndex = new AtomicInteger(0);

        @Override
        public <O> Function<O, ? extends Wrapper> generateMetaClass(Class<O> originClass) {
            return new WrapperGenerator<>(classIndex.incrementAndGet());
        }

        private class WrapperGenerator<O> implements Function<O, Wrapper> {

            private final int classIndex;
            private final AtomicInteger objectIndex = new AtomicInteger(0);

            WrapperGenerator(int classIndex) {
                this.classIndex = classIndex;
            }

            @Override
            public Wrapper apply(O o) {
                return new WrapperImpl(classIndex, objectIndex.incrementAndGet());
            }
        }
    }
}
