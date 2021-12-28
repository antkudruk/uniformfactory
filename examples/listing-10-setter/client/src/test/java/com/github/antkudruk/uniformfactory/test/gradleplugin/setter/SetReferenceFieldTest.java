package com.github.antkudruk.uniformfactory.test.gradleplugin.setter;

import com.github.antkudruk.uniformfactory.test.gradleplugin.setter.domain.OriginWithReferenceFieldImpl;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;

public class SetReferenceFieldTest {
    @Test
    public void test() {
        OriginWithReferenceFieldImpl originImpl = new OriginWithReferenceFieldImpl();

        Origin origin = (Origin) originImpl;

        origin.getAdapter().setValue("10");
        assertEquals((Integer) 10, originImpl.getValue());
    }
}
