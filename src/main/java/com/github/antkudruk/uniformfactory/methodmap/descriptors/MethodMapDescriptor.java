/*
    Copyright 2020 - 2022 Anton Kudruk

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.github.antkudruk.uniformfactory.methodmap.descriptors;

import com.github.antkudruk.uniformfactory.base.AbstractMethodCollectionDescriptor;
import com.github.antkudruk.uniformfactory.base.Enhancer;
import com.github.antkudruk.uniformfactory.classfactory.ChildMethodDescriptionBuilderWrapper;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.base.exception.WrongTypeException;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.methodmap.enhancers.MethodMapEnhancer;
import lombok.experimental.Delegate;
import net.bytebuddy.description.type.TypeDescription;

import java.lang.reflect.Method;
import java.util.Map;

/**
 *
 * Supposing origin class has multiple methods marked with {@code markerAnnotation}.
 * Method defined with {@code MethodMapDescriptor} returns a map of objects that
 * have methods calling corresponding rigin method.
 *
 * @param <F> Type of the map values
 *
 */
public class MethodMapDescriptor<F> extends AbstractMethodCollectionDescriptor<F> {

    private final MapElementSource<F> mapElementSource;

    public MethodMapDescriptor(Method wrapperMethod,
                               Class<F> functionalInterface,
                               MapElementSource<F> mapElementSource) {
        super(wrapperMethod, functionalInterface);
        this.mapElementSource = mapElementSource;
        validate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enhancer getEnhancer(TypeDescription originType) throws ClassGeneratorException {
        return new MethodMapEnhancer<>(
                "values",
                originType,
                wrapperMethod,
                mapElementSource.memberEntries(originType));
    }

    private void validate() {
        if (!Map.class.isAssignableFrom(wrapperMethod.getReturnType())) {
            throw new WrongTypeException(Map.class, wrapperMethod.getReturnType());
        }
    }

    @SuppressWarnings("unchecked")
    public static abstract class AbstractBuilder<F, T extends AbstractBuilder<F, T>>
            extends AbstractMethodCollectionDescriptor.AbstractBuilder<F, T>
            implements AnnotationMapElementSource.ShortcutBuilder.ParentBuilder<F>
    {
        private final Class<F> functionalInterface;
        private MapElementSource<F> mapElementSource;
        public AbstractBuilder(Class<F> functionalInterface, Method wrapperMethod) {
            super(wrapperMethod, functionalInterface);
            this.functionalInterface = functionalInterface;
        }

        public T setMapElementSource(MapElementSource<F> mapElementSource) {
            this.mapElementSource = mapElementSource;
            return (T) this;
        }

        public AnnotationMapElementSource.ShortcutBuilder<T, F> annotationMapElementSource() {
            return new AnnotationMapElementSource.ShortcutBuilder<>((T) this, functionalInterface);
        }

        @Override
        public MethodMapDescriptor<F> build() {
            return new MethodMapDescriptor<>(
                    wrapperMethod,
                    functionalInterface,
                    mapElementSource);
        }
    }

    public static final class Builder<F> extends AbstractBuilder<F, Builder<F>> {
        public Builder(Class<F> functionalInterface, Method wrapperMethod) {
            super(functionalInterface, wrapperMethod);
        }
    }

    /**
     * Map of methods
     *
     * @param <F> Type of a list element
     */
    public static class ShortcutBuilder<W, F>
            extends AbstractBuilder<F, ShortcutBuilder<W, F>> {

        @Delegate
        private final ChildMethodDescriptionBuilderWrapper<W> classFactoryReference;

        public ShortcutBuilder(
                ClassFactory.Builder<W> wrapperClass,
                Class<F> functionalInterface,
                Method wrapperMethod) {
            super(functionalInterface, wrapperMethod);
            classFactoryReference = new ChildMethodDescriptionBuilderWrapper<>(wrapperClass, this);
        }
    }
}
