package com.github.antkudruk.uniformfactory.singleton.atomicaccessor.constant;

import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.Constants;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;

import java.lang.reflect.Method;

import static junit.framework.TestCase.assertEquals;

public class ReturnConstantValueTest {

    private static final String ORIGIN_FIELD_VALUE = "origin field value";

    public static class OriginImpl {
        public String field = ORIGIN_FIELD_VALUE;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void generateFieldAccessClassTest() throws ReflectiveOperationException {

        String testValue = "value";

        Class atomClass = ReturnConstantValue.INSTANCE.generateClass(
                new TypeDescription.ForLoadedType(OriginImpl.class), testValue)
                .load(getClass().getClassLoader())
                .getLoaded();

        OriginImpl origin = new OriginImpl();

        Method method = atomClass.getDeclaredMethod(Constants.METHOD_NAME);
        Object atom = atomClass.getConstructor(OriginImpl.class).newInstance(origin);

        Object result = method.invoke(atom);

        assertEquals(testValue, result);
    }
}
