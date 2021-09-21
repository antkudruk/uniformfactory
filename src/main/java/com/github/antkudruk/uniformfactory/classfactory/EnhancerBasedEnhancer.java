package com.github.antkudruk.uniformfactory.classfactory;

import com.github.antkudruk.uniformfactory.base.Enhancer;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;

import java.util.Collection;

/**
 * Enhances dynamic classes based on the other enhancers. The class doesn't
 * take on validation on implemented members.
 */
public class EnhancerBasedEnhancer implements Enhancer {

    public final Collection<Enhancer> enhancers;

    public EnhancerBasedEnhancer(Collection<Enhancer> enhancers) {
        this.enhancers = enhancers;
    }

    @Override
    public Implementation.Composable addStaticInitiation(Implementation.Composable existingImplementation) {
        for (Enhancer enhancer: enhancers) {
            existingImplementation = enhancer.addStaticInitiation(existingImplementation);
        }
        return existingImplementation;
    }

    @Override
    public Implementation.Composable addInitiation(Implementation.Composable composable) {
        for(Enhancer enhancer: enhancers) {
            composable = enhancer.addInitiation(composable);
        }

        return composable;
    }

    @Override
    public <W> DynamicType.Builder<W> addMethod(DynamicType.Builder<W> bbBuilder) {
        for (Enhancer enhancer : enhancers) {
            bbBuilder = enhancer.addMethod(bbBuilder);
        }

        return bbBuilder;
    }
}
