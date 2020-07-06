package com.github.antkudruk.uniformfactory.test.gradleplugin.simplewrapper;

import com.github.antkudruk.uniformfactory.test.gradleplugin.simplewrapper.origins.OriginUsingField;
import com.github.antkudruk.uniformfactory.test.gradleplugin.simplewrapper.origins.OriginUsingMethod;
import com.github.antkudruk.uniformfactory.test.gradleplugin.simplewrapper.wrapper.Origin;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimpleWrapperTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    public void test() {
        OriginUsingField originUsingField1 = new OriginUsingField(100);
        OriginUsingField originUsingField2 = new OriginUsingField(-20);
        OriginUsingField originUsingField3 = new OriginUsingField(50);

        OriginUsingMethod originUsingMethod1 = new OriginUsingMethod("Alpha");
        OriginUsingMethod originUsingMethod2 = new OriginUsingMethod("Beta");
        OriginUsingMethod originUsingMethod3 = new OriginUsingMethod("Gamma");

        Assert.assertEquals("100", ((Origin) originUsingField1).getWrapper().getId());
        assertEquals("-20", ((Origin) originUsingField2).getWrapper().getId());
        assertEquals("50", ((Origin) originUsingField3).getWrapper().getId());

        assertEquals("Alpha", ((Origin) originUsingMethod1).getWrapper().getId());
        assertEquals("Beta", ((Origin) originUsingMethod2).getWrapper().getId());
        assertEquals("Gamma", ((Origin) originUsingMethod3).getWrapper().getId());
    }
}
