package com.github.antkudruk.uniformfactory.test.gradleplugin.calendar;

import com.github.antkudruk.uniformfactory.pluginbuilder.DefaultMetaClassFactory;
import com.github.antkudruk.uniformfactory.test.gradleplugin.calendar.wrapper.HasInterval;
import com.github.antkudruk.uniformfactory.test.gradleplugin.calendar.wrapper.Interval;
import com.github.antkudruk.uniformfactory.pluginbuilder.WrapperPlugin;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.singleton.descriptors.MethodSingletonDescriptor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CalendarClassFactory {

    private static final ClassFactory<Interval> classFactory ;
    static {
        try {
            classFactory = new ClassFactory.Builder<>(Interval.class)
                    .addMethodDescriptor(new MethodSingletonDescriptor.ShortcutBuilder<>(
                            Interval.class.getMethod("getNested"),
                            List.class)
                            .setMarkerAnnotation(Nested.class)
                            .build())
                    .build();
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static List<Interval> mapOriginToWrapperCollection(Collection<HasInterval> origin) {
        return origin.stream().map(HasInterval::getInterval).collect(Collectors.toList());
    }

    public static WrapperPlugin wrapperPlugin() {
        return new WrapperPlugin.Builder<>(Interval.class)
                .setOriginInterface(HasInterval.class)
                .setTypeMarker(IsTimeInterval.class)
                .setClassFactoryGenerator(CtorMeta.class)
                .build();
    }

    public static class CtorMeta extends DefaultMetaClassFactory<Interval> {
        public CtorMeta() {
            super(classFactory);
        }
    }
}
