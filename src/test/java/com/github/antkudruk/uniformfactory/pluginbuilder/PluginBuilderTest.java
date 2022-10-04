package com.github.antkudruk.uniformfactory.pluginbuilder;

import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.AmbiguousGetWrapperMethodException;
import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.GetWrapperMethodWrongTypeException;
import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.OriginInterfaceNotDefinedException;
import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.SelectClassCriteriaNotDefinedException;
import com.github.antkudruk.uniformfactory.pluginbuilder.impls.Adaptor;
import com.github.antkudruk.uniformfactory.pluginbuilder.impls.Origin;
import org.junit.Test;

import java.util.function.Function;

public class PluginBuilderTest {

    @interface TypeMarker {

    }

    @SuppressWarnings("unused")
    public interface OriginWithTwoMethods {
        Adaptor getWrapper();

        Object getAdditionalValue();
    }

    interface Adaptor2 {

    }

    public static class TestWrapperMeta implements MetaClassFactory<Adaptor> {
        @Override
        public <O> Function<O, ? extends Adaptor> generateMetaClass(Class<O> originClass) {
            return null;
        }
    }

    public static class TestWrapper2Meta implements MetaClassFactory<Adaptor2> {
        @Override
        public <O> Function<O, ? extends Adaptor2> generateMetaClass(Class<O> originClass) {
            return null;
        }
    }

    @Test(expected = OriginInterfaceNotDefinedException.class)
    public void givenNoOriginInterface_whenBuild_thenThrowOriginInterfaceNotDefinedException() {
        // when
        new WrapperPlugin(getDefaultBuilder().setOriginInterface(null));
    }

    @Test(expected = SelectClassCriteriaNotDefinedException.class)
    public void givenNoClassSelectCriteria_whenBuild_thenThrowSelectClassCriteriaNotDefinedException() {
        // when
        new WrapperPlugin(getDefaultBuilder().setSelectClassCriteria(null));
    }

    @Test(expected = GetWrapperMethodWrongTypeException.class)
    public void givenWrongWrapperType_whenBuild_thenGetWrapperMethodWrongTypeException() {
        // when
        new WrapperPlugin(
                getDefaultBuilder()
                        .clearWrappers()
                        .addWrapper(Adaptor2.class)
                        .setClassFactoryGenerator(TestWrapper2Meta.class)
                        .setMethodName("get")
                        .endWrapperDescriptor()
        );
    }

    @Test(expected = AmbiguousGetWrapperMethodException.class)
    public void givenAmbiguousWrapper_whenBuild_thenThrowAmbiguousGetWrapperMethodException() {
        new WrapperPlugin(
                getDefaultBuilder()
                        .addWrapper(Adaptor2.class)
                        .setClassFactoryGenerator(TestWrapper2Meta.class)
                        .setMethodName("get")
                        .endWrapperDescriptor()
        );
    }

    private WrapperPlugin.Builder getDefaultBuilder() {
        return new WrapperPlugin
                .Builder()
                .setOriginInterface(Origin.class)
                .setTypeMarker(TypeMarker.class)
                .addWrapper(Adaptor.class)
                .setClassFactoryGenerator(TestWrapperMeta.class)
                .setMethodName("get")
                .endWrapperDescriptor();
    }
}
