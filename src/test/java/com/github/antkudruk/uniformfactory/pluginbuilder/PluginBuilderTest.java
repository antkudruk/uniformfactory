package com.github.antkudruk.uniformfactory.pluginbuilder;

import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.AmbiguousGetWrapperMethodException;
import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.GetWrapperMethodNotExistsException;
import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.GetWrapperMethodWrongTypeException;
import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.OriginInterfaceNotDefinedException;
import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.StaticConstructorGeneratorException;
import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.SelectClassCriteriaNotDefinedException;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class PluginBuilderTest {

    private static final String ORIGIN_INTERFACE = "originInterface";
    private static final String GET_WRAPPER = "getWrapper";


    private WrapperPlugin.Builder<Wrapper> getDefaultBuilder() {
        return new WrapperPlugin.Builder<>(Wrapper.class)
                .setOriginInterface(Origin.class)
                .setClassFactoryGenerator(TestWrapperMeta.class)
                .setTypeMarker(TypeMarker.class);
    }

    @interface TypeMarker {

    }

    @SuppressWarnings("unused")
    public interface Origin {
        Wrapper getWrapper();
    }

    @SuppressWarnings("unused")
    public interface OriginWithTwoMethods {
        Wrapper getWrapper();

        Object getAdditionalValue();
    }

    public interface Wrapper {

    }

    public static class TestWrapperMeta implements MetaClassFactory<Wrapper> {
        @Override
        public <O> Function<O, ? extends Wrapper> generateMetaClass(Class<O> originClass) {
            return null;
        }
    }
}
