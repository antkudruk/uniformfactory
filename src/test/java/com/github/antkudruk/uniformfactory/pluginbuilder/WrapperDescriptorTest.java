package com.github.antkudruk.uniformfactory.pluginbuilder;

import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.GetWrapperMethodNotExistsException;
import com.github.antkudruk.uniformfactory.pluginbuilder.impls.Adapter;
import com.github.antkudruk.uniformfactory.pluginbuilder.impls.MetaClassFactoryImpl;
import org.junit.Test;

public class WrapperDescriptorTest {

    private static final String METHOD_NAME = "method";
    private static final String FIELD_NAME = "field";
    private static final String CLASS_FACTORY_generator_FIELD_NAME = "classFactoryGeneratorFieldName";

    @Test
    public void test() {
        new WrapperDescriptor<>(
                METHOD_NAME,
                FIELD_NAME,
                CLASS_FACTORY_generator_FIELD_NAME,
                Adapter.class,
                MetaClassFactoryImpl.class
        );
    }

    @Test(expected = GetWrapperMethodNotExistsException.class)
    public void whenNoMethodName_thenThrowGetWrapperMethodNotExistsException() {
        new WrapperDescriptor<>(
                null,
                FIELD_NAME,
                CLASS_FACTORY_generator_FIELD_NAME,
                Adapter.class,
                MetaClassFactoryImpl.class
        );
    }

    // TODO: Probably, check in wrapper test
    /*
    @Test(expected = GetWrapperMethodNotExistsException.class)
    public void whenNotExistingMethodName_() {
        new WrapperDescriptor<>(
                "nonExisting",
                FIELD_NAME,
                CLASS_FACTORY_generator_FIELD_NAME,
                Adapter.class,
                MetaClassFactoryImpl.class
        );
    }*/
}
