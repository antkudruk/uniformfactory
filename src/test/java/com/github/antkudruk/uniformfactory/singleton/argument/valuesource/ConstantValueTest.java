package com.github.antkudruk.uniformfactory.singleton.argument.valuesource;

import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.partieldescriptor.PartialConstantDescriptor;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PartialConstantDescriptor.class, ConstantValue.class})
@Ignore
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
    public void getSourceTestWithSameType() throws Exception {

        PartialConstantDescriptor constantDescriptor
                = PowerMockito.mock(PartialConstantDescriptor.class);

        PowerMockito
                .whenNew(PartialConstantDescriptor.class)
                .withAnyArguments()
                .thenReturn(constantDescriptor);

        TypeDescription originParameterTypeDescription
                = new TypeDescription.ForLoadedType(Constant.class);

        Constant constant = new Constant();

        ConstantValue<Constant> parameterValue = new ConstantValue<>(constant);

        Assert.assertEquals(
                constantDescriptor,
                parameterValue.getSource(ORIGIN_INDEX, originParameterTypeDescription).orElseThrow(RuntimeException::new)
        );

        PowerMockito.verifyNew(PartialConstantDescriptor.class)
                .withArguments(eq(ORIGIN_INDEX), eq(constant));
    }
}
