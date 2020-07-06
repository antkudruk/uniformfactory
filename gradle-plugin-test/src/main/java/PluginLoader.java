import com.github.antkudruk.uniformfactory.test.gradleplugin.calendar.CalendarClassFactory;
import com.github.antkudruk.uniformfactory.test.gradleplugin.classgenerationsingleton.MetaClassClassFactoryGenerator;
import com.github.antkudruk.uniformfactory.test.gradleplugin.customparameter.WrapperMetaClassFactory;
import com.github.antkudruk.uniformfactory.test.gradleplugin.empty.EmptyWrapperPluginImpl;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodtreewrapper.MethodTreeWrapperClassFactory;
import com.github.antkudruk.uniformfactory.test.gradleplugin.simplewrapper.SimpleWrapperClassFactory;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class PluginLoader implements Plugin {

    private final List<Plugin> plugins = Arrays.asList(
            SimpleWrapperClassFactory.wrapperPlugin(),
            CalendarClassFactory.wrapperPlugin(),
            WrapperMetaClassFactory.wrapperPlugin(),
            MethodTreeWrapperClassFactory.wrapperPlugin(),
            MetaClassClassFactoryGenerator.wrapperPlugin()
            , new EmptyWrapperPluginImpl()
    );

    @Override
    public DynamicType.Builder<?> apply(
            DynamicType.Builder<?> builder,
            TypeDescription typeDescription,
            ClassFileLocator classFileLocator) {

        for(Plugin plugin: plugins) {
            if(plugin.matches(typeDescription)) {
                try {
                    builder = plugin.apply(builder, typeDescription, classFileLocator);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw ex;
                }
            }
        }

        return builder;
    }

    @Override
    public void close() throws IOException {
        for (Plugin plugin : plugins) {
            plugin.close();
        }
    }

    @Override
    public boolean matches(TypeDescription target) {
        return plugins.stream().anyMatch(t -> t.matches(target));
    }
}
