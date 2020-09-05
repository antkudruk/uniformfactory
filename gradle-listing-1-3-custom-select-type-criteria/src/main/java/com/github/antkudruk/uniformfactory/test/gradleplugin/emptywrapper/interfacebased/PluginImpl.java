package com.github.antkudruk.uniformfactory.test.gradleplugin.emptywrapper.interfacebased;

import com.github.antkudruk.uniformfactory.pluginbuilder.WrapperPlugin;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;

public class PluginImpl extends WrapperPlugin<Wrapper> {
    public PluginImpl() {
        super(
                Origin.class,
                Wrapper.class,
                td -> td.getInterfaces()
                        .stream()
                        .map(TypeDefinition::asErasure)
                        .anyMatch(new TypeDescription.ForLoadedType(Origin.class)::equals),
                "examplePlugin",
                ClassFactoryGeneratorImpl.class);
    }
}
