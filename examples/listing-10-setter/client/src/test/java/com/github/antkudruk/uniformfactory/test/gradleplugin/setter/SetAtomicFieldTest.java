package com.github.antkudruk.uniformfactory.test.gradleplugin.setter;

import com.github.antkudruk.uniformfactory.test.gradleplugin.setter.domain.OriginWithAtomicField;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class SetAtomicFieldTest {
    @Test
    public void test() {
        OriginWithAtomicField originImpl = new OriginWithAtomicField();

        Origin origin = (Origin) originImpl;

        origin.getAdapter().setValue("10");
        assertEquals((Integer) 10, originImpl.getValue());
    }
}
