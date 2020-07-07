/*
    Copyright 2020 Anton Kudruk

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

package com.github.antkudruk.uniformfactory.singleton.descriptors;

import com.github.antkudruk.uniformfactory.base.Enhancer;
import com.github.antkudruk.uniformfactory.base.AbstractMethodDescriptorImpl;
import com.github.antkudruk.uniformfactory.base.exception.WrongTypeException;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.singleton.enhancers.SingletonMethodToConstantEnhancer;
import com.github.antkudruk.uniformfactory.singleton.enhancers.SingletonMethodToFieldEnhancer;
import com.github.antkudruk.uniformfactory.singleton.enhancers.SingletonMethodToMethodEnhancer;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Describes method singleton.
 * Method Singleton maps a method of wrapper class to a method or field in
 * wrapper class.
 *
 */
public class MethodSingletonDescriptor<R> extends AbstractMethodDescriptorImpl {

    private static final String FIELD_NAME_PREFIX = "singletonMethod";
    private static final AtomicLong fieldNameIndex = new AtomicLong(0L);

    private final String fieldAccessorFieldName
            = FIELD_NAME_PREFIX + fieldNameIndex.incrementAndGet();

    private final boolean hasDefaultValue;
    private final R defaultValue;

    public MethodSingletonDescriptor(BuilderInterface<?, R> builder) {
        super(builder);
        this.hasDefaultValue = builder.hasDefaultValue();
        this.defaultValue = builder.getDefaultValue();
        validate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enhancer getEnhancer(TypeDescription originClass)
            throws ClassGeneratorException {

        MethodList<? extends MethodDescription> singletonOriginMethod =
                originClass.getDeclaredMethods()
                        .filter(ElementMatchers.isAnnotatedWith(markerAnnotation));

        FieldList<? extends FieldDescription> singletonOriginField =
                originClass.getDeclaredFields()
                        .filter(ElementMatchers.isAnnotatedWith(markerAnnotation));

        if ((singletonOriginMethod.size() > 1) || (singletonOriginField.size() > 1)
                || !(singletonOriginMethod.isEmpty() || singletonOriginField.isEmpty())) {
            throw new AmbiguousMethodException(markerAnnotation.getName(), null);
        }

        if (!singletonOriginMethod.isEmpty()) {
            MethodDescription originMethod = singletonOriginMethod.get(0);
            return new SingletonMethodToMethodEnhancer(
                    fieldAccessorFieldName,
                    originClass,
                    originMethod,
                    wrapperMethod,
                    parameterMapper,
                    resultMapper);
        } else if (!singletonOriginField.isEmpty()) {
            return new SingletonMethodToFieldEnhancer(
                    fieldAccessorFieldName,
                    originClass,
                    singletonOriginField.get(0),
                    wrapperMethod,
                    resultMapper);
        } else if (hasDefaultValue) {
            return new SingletonMethodToConstantEnhancer<>(
                    fieldAccessorFieldName,
                    originClass,
                    defaultValue,
                    wrapperMethod
            );
        }

        throw new RuntimeException("No default value specified for method "
                + wrapperMethod.getReturnType().getSimpleName()
                + " " + wrapperMethod.getName()
                + ". Either default value should be specified or a class member should be marked with @"
                + markerAnnotation.getName() + " annotation.");
    }

    private void validate() {
        if (wrapperMethod.getReturnType() != resultMapper.getWrapperReturnType()) {
            throw new WrongTypeException(wrapperMethod.getReturnType(), resultMapper.getWrapperReturnType());
        }
    }

    public interface BuilderInterface<M extends Annotation, R> extends AbstractMethodDescriptorImpl.BuilderInterface<M, R> {
        boolean hasDefaultValue();

        R getDefaultValue();
    }

    public static class Builder<M extends Annotation, R>
            extends AbstractMethodDescriptorImpl.Builder<M, R, MethodSingletonDescriptor.Builder<M, R>>
            implements BuilderInterface<M, R> {

        private boolean hasDefaultValue;
        private R defaultValue;

        public Builder(Class<M> markerAnnotation, Method wrapperMethod, Class<R> methodResultType) {
            super(markerAnnotation, wrapperMethod, methodResultType);
        }

        @Override
        public MethodSingletonDescriptor build() {
            return new MethodSingletonDescriptor<>(this);
        }

        @Override
        public boolean hasDefaultValue() {
            return hasDefaultValue;
        }

        @Override
        public R getDefaultValue() {
            return defaultValue;
        }

        public Builder<M, R> setDefaultValue(R defaultValue) {
            this.hasDefaultValue = true;
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder<M, R> dropDefaultValue() {
            this.hasDefaultValue = false;
            this.defaultValue = null;
            return this;
        }
    }

    public static abstract class IntermediateShortcutBuilder<M extends Annotation, R, T extends IntermediateShortcutBuilder<M, R, T>>
            extends AbstractMethodDescriptorImpl.ShortcutBuilder<M, R, T>
            implements BuilderInterface<M, R> {

        private boolean hasDefaultValue;
        private R defaultValue;

        public IntermediateShortcutBuilder(Class<M> markerAnnotation, Method wrapperMethod, Class<R> methodResultType) {
            super(markerAnnotation, wrapperMethod, methodResultType);
        }

        @Override
        public MethodSingletonDescriptor build() {
            return new MethodSingletonDescriptor<>(this);
        }

        @Override
        public boolean hasDefaultValue() {
            return hasDefaultValue;
        }

        @SuppressWarnings("unchecked")
        public T dropDefaultValue() {
            this.hasDefaultValue = false;
            this.defaultValue = null;
            return (T) this;
        }

        @Override
        public R getDefaultValue() {
            return defaultValue;
        }

        @SuppressWarnings("unchecked")
        public T setDefaultValue(R defaultValue) {
            this.hasDefaultValue = true;
            this.defaultValue = defaultValue;
            return (T) this;
        }
    }

    public static final class ShortcutBuilder<M extends Annotation, R>
            extends IntermediateShortcutBuilder<M, R, ShortcutBuilder<M, R>> {

        public ShortcutBuilder(Class<M> markerAnnotation, Method wrapperMethod, Class<R> methodResultType) {
            super(markerAnnotation, wrapperMethod, methodResultType);
        }
    }
}
