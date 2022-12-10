package com.github.antkudruk.uniformfactory.singleton.argument.valuesource;

import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialDescriptor;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Assert;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertFalse;

public class ConstantValueTest {

    private static final int ORIGIN_INDEX = 10;

    public static class Constant {

    }

    public static class OriginParameter {

    }

    @Test
    public void getSourceTestWithDifferentType() {

        TypeDescription originParameterTypeDescription
                = new TypeDescription.ForLoadedType(OriginParameter.class);

        Constant constant = new Constant();

        ConstantValue<Constant> parameterValue = new ConstantValue<>(constant);

        assertFalse(parameterValue.getSource(ORIGIN_INDEX, originParameterTypeDescription).isPresent());
    }

    @Test
    public void whenGetSource_thenReturnPartialParameterDescriptor() {
        // given
        TypeDescription originParameterTypeDescription
                = new TypeDescription.ForLoadedType(Constant.class);

        Constant constant = new Constant();

        ConstantValue<Constant> parameterValue = new ConstantValue<>(constant);

        // when
        PartialDescriptor descriptor = parameterValue
                .getSource(ORIGIN_INDEX, originParameterTypeDescription)
                .orElseThrow(RuntimeException::new);

        // then
        Assert.assertEquals(ORIGIN_INDEX, descriptor.getOriginIndex());
        Assert.assertEquals(
                constant,
                Whitebox.getInternalState(descriptor, "constant"));
    }
}
