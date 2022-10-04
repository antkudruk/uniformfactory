package com.github.antkudruk.uniformfactory.pluginbuilder;

import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.*;
import com.github.antkudruk.uniformfactory.pluginbuilder.impls.Adaptor;
import com.github.antkudruk.uniformfactory.pluginbuilder.impls.MetaClassFactoryImpl;
import com.github.antkudruk.uniformfactory.pluginbuilder.impls.Origin;
import org.junit.Test;

public class WrapperDescriptorTest {

    private static final String METHOD_NAME = "method";
    private static final String WRAPPER_FIELD_NAME = "field";
    private static final String CLASS_FACTORY_GENERATOR_FIELD_NAME = "classFactoryGeneratorFieldName";

    @Test
    public void test() {
        new WrapperDescriptor<>(
                METHOD_NAME,
                WRAPPER_FIELD_NAME,
                CLASS_FACTORY_GENERATOR_FIELD_NAME,
                Adaptor.class,
                MetaClassFactoryImpl.class
        );
    }

    @Test(expected = MethodNameNotSpecifiedException.class)
    public void whenNoMethodName_thenThrowGetWrapperMethodNotExistsException() {
        // when
        new WrapperDescriptor<>(
                null,
                WRAPPER_FIELD_NAME,
                CLASS_FACTORY_GENERATOR_FIELD_NAME,
                Adaptor.class,
                MetaClassFactoryImpl.class
        );
    }

    @Test(expected = WrapperFieldNotSpecifiedException.class)
    public void whenNoWrapperField_thenThrowWrapperFieldNotSpecifiedException() {
        // when
        new WrapperDescriptor<>(
                METHOD_NAME,
                null,
                CLASS_FACTORY_GENERATOR_FIELD_NAME,
                Adaptor.class,
                MetaClassFactoryImpl.class
        );
    }

    @Test(expected = WrapperFactoryFieldNotSpecifiedException.class)
    public void whenNoWrapperFactoryField_thenThrowWrapperFieldNotSpecifiedException() {
        // when
        new WrapperDescriptor<>(
                METHOD_NAME,
                WRAPPER_FIELD_NAME,
                null,
                Adaptor.class,
                MetaClassFactoryImpl.class
        );
    }

    @Test(expected = NoWrapperClassSpecifiedException.class)
    public void whenNoWrapperClass_thenThrow() {
        // when
        new WrapperDescriptor<>(
                METHOD_NAME,
                WRAPPER_FIELD_NAME,
                CLASS_FACTORY_GENERATOR_FIELD_NAME,
                null,
                MetaClassFactoryImpl.class
        );
    }

    @Test(expected = NoClassFactoryException.class)
    public void whenNoMetaClassFactory_thenThrowNoClassFactoryException() {
        // when
        new WrapperDescriptor<>(
                METHOD_NAME,
                WRAPPER_FIELD_NAME,
                CLASS_FACTORY_GENERATOR_FIELD_NAME,
                Adaptor.class,
                null
        );
    }

    @Test(expected = GetWrapperMethodNotExistsException.class)
    public void givenNotExistingMethodName_whenValidateForOrigin_thenThrowGetWrapperMethodNotExistsException() {
        // given
        WrapperDescriptor<Adaptor> wrapperDescriptor = new WrapperDescriptor<>(
                "nonExisting",
                WRAPPER_FIELD_NAME,
                CLASS_FACTORY_GENERATOR_FIELD_NAME,
                Adaptor.class,
                MetaClassFactoryImpl.class
        );

        //  when
        wrapperDescriptor.validateForOrigin(Origin.class);
    }

    @Test(expected = GetWrapperMethodWrongTypeException.class)
    public void givenReturnTypeDoesNotCoincideWithWrapper_whenValidateForOrigin_thenThrowGetWrapperMethodWrongTypeException() {
        // given
        WrapperDescriptor<Adaptor> wrapperDescriptor = new WrapperDescriptor<>(
                "getString",
                WRAPPER_FIELD_NAME,
                CLASS_FACTORY_GENERATOR_FIELD_NAME,
                Adaptor.class,
                MetaClassFactoryImpl.class
        );

        //  when
        wrapperDescriptor.validateForOrigin(Origin.class);
    }
}
