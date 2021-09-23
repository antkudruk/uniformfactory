package com.github.antkudruk.uniformfactory.test.gradleplugin.stateful;

import com.github.antkudruk.uniformfactory.pluginbuilder.WrapperDescriptor;
import com.github.antkudruk.uniformfactory.pluginbuilder.WrapperPlugin;
import net.bytebuddy.description.type.TypeDescription;
import java.util.List;

public class PluginImpl extends WrapperPlugin {

    public PluginImpl() {

        super(
                Origin.class,
                PluginImpl::modifyType,
                List.of(

                        new WrapperDescriptor<>(
                                "getAdapter",
                                "adapter",
                                "adapterFactory",
                                Wrapper.class,
                                WrapperFactoryImpl.class),

                        new WrapperDescriptor<>(
                                "getState",
                                "state",
                                "stateFactory",
                                State.class,
                                StateFactoryImpl.class)

                ));
    }

    private static boolean modifyType(TypeDescription td) {
        return td.getDeclaredAnnotations().isAnnotationPresent(Marker.class);
    }
}
