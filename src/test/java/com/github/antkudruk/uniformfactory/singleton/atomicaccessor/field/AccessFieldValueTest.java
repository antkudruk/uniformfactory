package com.github.antkudruk.uniformfactory.singleton.atomicaccessor.field;

import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.Constants;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

public class AccessFieldValueTest {

    private static final String ORIGIN_FIELD_VALUE = "origin field value";
    private static final String WRAPPER_METHOD_RESULT = "wrapper method result";

    public static class OriginImpl {
        public String field = ORIGIN_FIELD_VALUE;
    }

    public interface Wrapper {
        String methodReturningString();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void generateFieldAccessClassTest() throws ReflectiveOperationException {
        Function resultTranslatorMock = Mockito.mock(Function.class);
        Mockito.when(resultTranslatorMock.apply(eq(ORIGIN_FIELD_VALUE)))
                .thenReturn(WRAPPER_METHOD_RESULT);

        // when
        Class atomClass = AccessFieldValue.INSTANCE.generateClass(
                    new TypeDescription.ForLoadedType(OriginImpl.class),
                    resultTranslatorMock,
                    new FieldDescription.ForLoadedField(
                            OriginImpl.class.getField("field")),
                    Wrapper.class.getDeclaredMethod("methodReturningString")
                )
                .load(getClass().getClassLoader())
                .getLoaded();

        OriginImpl origin = new OriginImpl();

        Method method = atomClass.getDeclaredMethod(Constants.METHOD_NAME);
        Object atom = atomClass.getConstructor(OriginImpl.class).newInstance(origin);

        Object result = method.invoke(atom);

        // then
        Mockito.verify(resultTranslatorMock, times(1))
                .apply(ORIGIN_FIELD_VALUE);

        assertEquals(WRAPPER_METHOD_RESULT, result);
    }
}
