package com.github.antkudruk.uniformfactory.container;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WrapperFactoryTest {

    public interface Wrapper {

    }

    public static class Type0 {

    }

    public static class Type1 {

    }

    @Test
    public void givenOneElement_whenRegisterNewClass_thenAdd() throws ClassGeneratorException {
        // given
        Wrapper wrapper = mock(Wrapper.class);
        ClassFactory<Wrapper> classFactory = mock(ClassFactory.class);
        Function<Type0, Wrapper> wrapperForClassContainer = mock(Function.class);
        Type0 gameObject = new Type0();
        when(wrapperForClassContainer.apply(eq(gameObject)))
                .thenReturn(wrapper);
        Map<Class<?>, Function<?, Wrapper>> classContainers = new HashMap<Class<?>, Function<?, Wrapper>>() {{
            put(Type0.class, wrapperForClassContainer);
        }};
        WrapperFactoryImpl<Wrapper> testSubject = new WrapperFactoryImpl<>(
                classFactory,
                classContainers);

        // when
        Wrapper result = testSubject.get(gameObject);

        // then
        assertEquals(wrapper, result);
        assertEquals(wrapperForClassContainer, classContainers.get(Type0.class));
        assertEquals(1, classContainers.size());
        verify(wrapperForClassContainer, times(1)).apply(eq(gameObject));
    }

    @Test
    public void givenOneElement_whenRegisterExistingClass_thenAdd() throws ClassGeneratorException {
        // given
        Wrapper wrapper = mock(Wrapper.class);
        Function<Type0, Wrapper> wrapperForClass0Container = mock(Function.class);
        Map<Class<?>, Function<?, Wrapper>> classContainers = new HashMap() {{
            put(Type0.class, wrapperForClass0Container);
        }};
        Type1 gameObject = new Type1();
        Function<Type1, Wrapper> gameObjectsForClass1Container = mock(Function.class);
        when(gameObjectsForClass1Container.apply(eq(gameObject)))
                .thenReturn(wrapper);
        ClassFactory<Wrapper> containerFactory = mock(ClassFactory.class);
        when(containerFactory.buildWrapperFactory(Type1.class)).thenReturn(gameObjectsForClass1Container);
        WrapperFactoryImpl<Wrapper> testSubject = new WrapperFactoryImpl<>(
                containerFactory,
                classContainers);

        // when
        Wrapper result = testSubject.get(gameObject);

        // then
        assertEquals(wrapper, result);
        assertEquals(wrapperForClass0Container, classContainers.get(Type0.class));
        assertEquals(gameObjectsForClass1Container, classContainers.get(Type1.class));
        verify(gameObjectsForClass1Container, times(1)).apply(eq(gameObject));
    }
}
