package com.github.antkudruk.uniformfactory.base.bytecode;

import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;
import org.junit.Test;
import org.mockito.Mockito;

import static com.github.antkudruk.uniformfactory.common.TypeDescriptionShortcuts.findConstructor;

public class ReturnConstructedValueImplementationTest {

    public interface Parameter {
        void invoke();
    }

    interface Bar {

    }

    public static class BarImpl implements Bar {
        public BarImpl(Parameter parameter) {
            parameter.invoke();
        }
    }

    public ReturnConstructedValueImplementation testSubject
            = new ReturnConstructedValueImplementation(
                    new TypeDescription.ForLoadedType(BarImpl.class),
                    new TypeDescription.ForLoadedType(Parameter.class)
            );

    @Test
    @SneakyThrows({ReflectiveOperationException.class})
    public void constructedValueTest() {
        // given
        Parameter parameter = Mockito.mock(Parameter.class);
        Mockito.doNothing().when(parameter).invoke();

        // when
        Class<?> testType = new ByteBuddy()
                .subclass(Object.class, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                .defineConstructor(Visibility.PUBLIC)
                .intercept(MethodCall.invoke(findConstructor(Object.class).orElseThrow(RuntimeException::new)))

                .defineMethod("create", BarImpl.class, Visibility.PUBLIC)
                .withParameters(Parameter.class)
                .intercept(testSubject)

                .make()
                .load(getClass().getClassLoader())
                .getLoaded();

        Object testObject = testType.getConstructor().newInstance();

        testType.getDeclaredMethod("create", Parameter.class).invoke(testObject, parameter);

        // then
        Mockito.verify(parameter, Mockito.times(1)).invoke();
    }

    @Test
    public void givenParamNotAssignableToCtorParam_whenCreate_thenThrow() {
        // given

        // when

        // then
    }

    @Test
    public void givenClassNotAssignableToReturn_whenCreate_thenThrow() {
        // given

        // when

        // then
    }
}
