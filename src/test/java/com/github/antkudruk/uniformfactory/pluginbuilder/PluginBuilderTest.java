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

    @Test
    public void defaultValidBuilderTest() {
        WrapperPlugin wrapperPlugin = getDefaultBuilder().build();

        assertEquals(Origin.class,
                Whitebox.getInternalState(wrapperPlugin, ORIGIN_INTERFACE));
    }

    @Test
    public void customWrapperFieldsTest() {
        WrapperPlugin wrapperPlugin = getDefaultBuilder()
                .setWrapperFieldName("customWrapperField")
                .setWrapperClassFactoryFieldName("customWrapperConstructorField")
                .build();

        assertEquals(Origin.class,
                Whitebox.getInternalState(wrapperPlugin, ORIGIN_INTERFACE));
    }

    @Test
    public void validBuilderOnOriginWthTwoMethods() {
        //OriginWithTwoMethods
        WrapperPlugin wrapperPlugin = getDefaultBuilder()
                .setOriginInterface(OriginWithTwoMethods.class)
                .setGetWrapperMethodName(GET_WRAPPER)
                .build();

        assertEquals(OriginWithTwoMethods.class,
                Whitebox.getInternalState(wrapperPlugin, ORIGIN_INTERFACE));
    }

    @Test(expected = AmbiguousGetWrapperMethodException.class)
    public void ambiguousGetWrapperMethodExceptionTest() {
        getDefaultBuilder()
                .setOriginInterface(OriginWithTwoMethods.class)
                .build();
    }

    @Test(expected = GetWrapperMethodNotExistsException.class)
    public void nonexistingGetWrapperMethodTest() {
        getDefaultBuilder()
                .setGetWrapperMethodName("nonexistingMethod")
                .build();
    }

    @Test(expected = GetWrapperMethodWrongTypeException.class)
    public void wrapperMethodReturnsWrongValueTest() {
        getDefaultBuilder()
                .setOriginInterface(OriginWithTwoMethods.class)
                .setGetWrapperMethodName("getAdditionalValue")
                .build();
    }

    @Test(expected = StaticConstructorGeneratorException.class)
    public void noStaticConstructorGeneratorSpecifiedTest() {
        getDefaultBuilder()
                .setClassFactoryGenerator(null)
                .build();
    }

    @Test(expected = OriginInterfaceNotDefinedException.class)
    public void originInterfaceNotDefinedTest() {
        getDefaultBuilder().setOriginInterface(null).build();
    }

    @Test(expected = SelectClassCriteriaNotDefinedException.class)
    public void typeMarkerNotDefinedTest() {
        getDefaultBuilder().setSelectClassCriteria(null).build();
    }

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
