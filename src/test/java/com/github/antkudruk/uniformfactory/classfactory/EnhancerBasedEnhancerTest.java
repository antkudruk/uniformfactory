package com.github.antkudruk.uniformfactory.classfactory;

import com.github.antkudruk.uniformfactory.base.Enhancer;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;

public class EnhancerBasedEnhancerTest <B> {
    @Test
    public void whenAddMethod_thenApplyEnhancer() {

        // given
        DynamicType.Builder<B> existing = mockDynamicTypeBuilder();
        DynamicType.Builder<B> updatedAfterFirstEnhancer = mockDynamicTypeBuilder();
        DynamicType.Builder<B> updatedAfterSecondEnhancer = mockDynamicTypeBuilder();

        Enhancer firstEnhancer = mockEnhancer(existing, updatedAfterFirstEnhancer);
        Enhancer secondEnhancer = mockEnhancer(updatedAfterFirstEnhancer, updatedAfterSecondEnhancer);

        EnhancerBasedEnhancer enhancerBasedEnhancer = new EnhancerBasedEnhancer(
                Arrays.asList(firstEnhancer, secondEnhancer));

        // when/then
        assertEquals(updatedAfterSecondEnhancer, enhancerBasedEnhancer.addMethod(existing));
    }

    @Test
    public void whenAddInitiation_thenApplyEnhancer() {

        // given
        Implementation.Composable existing = mockComposble();
        Implementation.Composable updatedAfterFirstEnhancer = mockComposble();
        Implementation.Composable updatedAfterSecondEnhancer = mockComposble();

        Enhancer firstEnhancer = mockEnhancerWithInitialization(
                existing, updatedAfterFirstEnhancer);
        Enhancer secondEnhancer = mockEnhancerWithInitialization(
                updatedAfterFirstEnhancer, updatedAfterSecondEnhancer);

        EnhancerBasedEnhancer enhancerBasedEnhancer = new EnhancerBasedEnhancer(
                Arrays.asList(firstEnhancer, secondEnhancer));

        // when/then
        assertEquals(updatedAfterSecondEnhancer, enhancerBasedEnhancer.addInitiation(existing));
    }

    @Test
    public void whenAddStaticInitiation_thenApplyEnhancer() {

        // given
        Implementation.Composable existing = mockComposble();
        Implementation.Composable updatedAfterFirstEnhancer = mockComposble();
        Implementation.Composable updatedAfterSecondEnhancer = mockComposble();

        Enhancer firstEnhancer = mockEnhancerWithStaticInitialization(
                existing, updatedAfterFirstEnhancer);
        Enhancer secondEnhancer = mockEnhancerWithStaticInitialization(
                updatedAfterFirstEnhancer, updatedAfterSecondEnhancer);

        EnhancerBasedEnhancer enhancerBasedEnhancer = new EnhancerBasedEnhancer(
                Arrays.asList(firstEnhancer, secondEnhancer));

        // when/then
        assertEquals(updatedAfterSecondEnhancer, enhancerBasedEnhancer.addStaticInitiation(existing));
    }

    private <E> Enhancer mockEnhancer(DynamicType.Builder<E> existing, DynamicType.Builder<E> updated) {
        Enhancer mock = Mockito.mock(Enhancer.class);
        Mockito.when(mock.addMethod(Mockito.eq(existing))).thenReturn(updated);
        return mock;
    }

    private DynamicType.Builder<B> mockDynamicTypeBuilder() {
        //noinspection unchecked
        return Mockito.mock(DynamicType.Builder.class);
    }

    private Enhancer mockEnhancerWithInitialization(
            Implementation.Composable existing,
            Implementation.Composable updated) {

        Enhancer mock = Mockito.mock(Enhancer.class);
        Mockito.when(mock.addInitiation(Mockito.eq(existing))).thenReturn(updated);
        return mock;
    }

    private Enhancer mockEnhancerWithStaticInitialization(
            Implementation.Composable existing,
            Implementation.Composable updated) {

        Enhancer mock = Mockito.mock(Enhancer.class);
        Mockito.when(mock.addStaticInitiation(Mockito.eq(existing))).thenReturn(updated);
        return mock;
    }

    private Implementation.Composable mockComposble() {
        return Mockito.mock(Implementation.Composable.class);
    }
}
