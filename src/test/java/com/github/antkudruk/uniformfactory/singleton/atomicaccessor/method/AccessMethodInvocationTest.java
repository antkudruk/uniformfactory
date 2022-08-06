package com.github.antkudruk.uniformfactory.singleton.atomicaccessor.method;

import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialDescriptor;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.partieldescriptor.PartialParameterDescriptor;
import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.Constants;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class AccessMethodInvocationTest {

    public static class OriginImpl {
        @SuppressWarnings("unused")
        public String concat(String name, Integer index) {
            return name + " " + index;
        }
    }

    public interface Wrapper {
        String concat(String name, Integer index);
    }

    @Test
    public void test() throws ReflectiveOperationException {
        TypeDescription originTypeDescription = new TypeDescription.ForLoadedType(OriginImpl.class);

        Class<?> wrapperClass = AccessMethodInvocation.INSTANCE.generateClass(
                originTypeDescription,
                (Object o) -> reverse((String) o),
                originTypeDescription.getDeclaredMethods()
                        .filter(ElementMatchers.nameContains("concat"))
                        .getOnly(),
                Wrapper.class.getDeclaredMethod("concat", String.class, Integer.class),
                Arrays.asList(new PartialDescriptor[]{
                        new PartialParameterDescriptor<>(0, 0, (Object o) -> ((String) o).toUpperCase()),
                        new PartialParameterDescriptor<>(1, 1, (Object i) -> 10 - (Integer) i)
                })
        ).load(getClass().getClassLoader()).getLoaded();

        OriginImpl origin = new OriginImpl();
        Object wrapperObject = wrapperClass.getConstructor(OriginImpl.class).newInstance(origin);

        Object result = wrapperClass.getDeclaredMethod(Constants.METHOD_NAME, String.class, Integer.class)
                .invoke(wrapperObject, "alpha", 3);

        assertEquals("7 AHPLA", result);
    }

    private String reverse(String s) {
        String result = "";
        for (int i = 0; i < s.length(); i++) {
            result = s.substring(i, i + 1).concat(result);
        }
        return result;
    }
}
