/*
package com.github.antkudruk.uniformfactory.pluginbuilder;

import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.*;
import com.github.antkudruk.uniformfactory.pluginbuilder.impls.Adapter;
import com.github.antkudruk.uniformfactory.pluginbuilder.impls.MetaClassFactoryImpl;
import com.github.antkudruk.uniformfactory.pluginbuilder.impls.Origin;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;

public class WrapperPluginTest {

    @Test(expected = OriginInterfaceNotDefinedException.class)
    public void givenNullOriginInterface_thenThrow() {
        new WrapperPlugin(getDefaultBuilder().setOriginInterface(null));
    }


    @Test
    public void defaultValidBuilderTest() {
        WrapperPlugin wrapperPlugin = getDefaultBuilder().build();

        assertEquals(PluginBuilderTest.Origin.class,
                Whitebox.getInternalState(wrapperPlugin, ORIGIN_INTERFACE));
    }

    @Test
    public void customWrapperFieldsTest() {
        WrapperPlugin wrapperPlugin = getDefaultBuilder()
                .setWrapperFieldName("customWrapperField")
                .setWrapperClassFactoryFieldName("customWrapperConstructorField")
                .build();

        assertEquals(PluginBuilderTest.Origin.class,
                Whitebox.getInternalState(wrapperPlugin, ORIGIN_INTERFACE));
    }

    @Test
    public void validBuilderOnOriginWthTwoMethods() {
        //OriginWithTwoMethods
        WrapperPlugin wrapperPlugixn = getDefaultBuilder()
                .setOriginInterface(PluginBuilderTest.OriginWithTwoMethods.class)
                .setGetWrapperMethodName(GET_WRAPPER)
                .build();

        assertEquals(PluginBuilderTest.OriginWithTwoMethods.class,
                Whitebox.getInternalState(wrapperPlugin, ORIGIN_INTERFACE));
    }

    @Test(expected = AmbiguousGetWrapperMethodException.class)
    public void ambiguousGetWrapperMethodExceptionTest() {
        getDefaultBuilder()
                .setOriginInterface(PluginBuilderTest.OriginWithTwoMethods.class)
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
                .setOriginInterface(PluginBuilderTest.OriginWithTwoMethods.class)
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

    private WrapperPlugin.Builder<Adapter> getDefaultBuilder() {
        return new WrapperPlugin.Builder<>(Adapter.class)
                .setOriginInterface(Origin.class)
                .setGetWrapperMethodName("get")
                .setSelectClassCriteria(m -> false)
                .setWrapperFieldName("wrapper")
                .setWrapperClassFactoryFieldName("classFactory")
                .setClassFactoryGenerator(MetaClassFactoryImpl.class);
    }
}
*/