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

package com.github.antkudruk.uniformfactory.singleton.descriptors;

import com.github.antkudruk.uniformfactory.base.AbstractMethodWithMappersDescriptorImpl;
import com.github.antkudruk.uniformfactory.base.Enhancer;
import com.github.antkudruk.uniformfactory.base.TypeShortcuts;
import com.github.antkudruk.uniformfactory.base.exception.WrongTypeException;
import com.github.antkudruk.uniformfactory.classfactory.ChildMethodDescriptionBuilderWrapper;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.MemberSelector;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.ParameterBindersSource;
import com.github.antkudruk.uniformfactory.singleton.enhancers.SingletonMethodToConstantEnhancer;
import com.github.antkudruk.uniformfactory.singleton.enhancers.SingletonMethodToFieldEnhancer;
import com.github.antkudruk.uniformfactory.singleton.enhancers.SingletonMethodToMethodEnhancer;
import lombok.experimental.Delegate;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Describes method singleton.
 * Method Singleton maps a method of wrapper class to a method or field in
 * wrapper class.
 *
 */
public class MethodSingletonDescriptor<R> extends AbstractMethodWithMappersDescriptorImpl {

    private static final String FIELD_NAME_PREFIX = "singletonMethod";
    private static final AtomicLong fieldNameIndex = new AtomicLong(0L);

    private final String fieldAccessorFieldName
            = FIELD_NAME_PREFIX + fieldNameIndex.incrementAndGet();

    protected final ResultMapperCollection<R> resultMapper;
    protected final boolean hasDefaultValue;
    protected final R defaultValue;

    public MethodSingletonDescriptor(
            Method wrapperMethod,
            MemberSelector memberSelector,
            ParameterBindersSource parameterMapper,
            ResultMapperCollection<R> resultMapper,
            R defaultValue,
            boolean hasDefaultValue
    ) {
        super(wrapperMethod, memberSelector, parameterMapper);
        this.resultMapper = resultMapper;
        this.defaultValue = defaultValue;
        this.hasDefaultValue = hasDefaultValue;
        validate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enhancer getEnhancer(TypeDescription originClass)
            throws ClassGeneratorException {

        List<MethodDescription> singletonOriginMethod = memberSelector.getMethods(originClass);
        List<FieldDescription> singletonOriginField = memberSelector.getFields(originClass);

        if ((singletonOriginMethod.size() > 1) || (singletonOriginField.size() > 1)
                || !(singletonOriginMethod.isEmpty() || singletonOriginField.isEmpty())) {
            throw new AmbiguousMethodException(null);
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
                + ". Either default value should be specified or a member selected should provide one member.");
    }

    private void validate() {
        if (TypeShortcuts.getBoxedType(wrapperMethod.getReturnType()) != resultMapper.getWrapperReturnType()) {
            throw new WrongTypeException(wrapperMethod.getReturnType(), resultMapper.getWrapperReturnType());
        }
    }


    @SuppressWarnings("unchecked")
    public static abstract class AbstractBuilder<R, T extends AbstractBuilder<R, T>>
            extends AbstractMethodWithMappersDescriptorImpl.AbstractBuilder<T> {

        private boolean hasDefaultValue;
        private R defaultValue;
        private ResultMapperCollection<R> resultMapper;

        public AbstractBuilder(Method wrapperMethod, Class<R> methodResultType) {
            super(wrapperMethod);
            this.resultMapper = new ResultMapperCollection<>(methodResultType);
        }

        /**
         * {inheritDoc}
         */

        @Override
        public MethodSingletonDescriptor<R> build() {
            return new MethodSingletonDescriptor<>(
                    wrapperMethod,
                    getMemberSelector(),
                    getParameterMapper(),
                    resultMapper,
                    defaultValue,
                    hasDefaultValue
            );
        }

        public T setDefaultValue(R defaultValue) {
            this.hasDefaultValue = true;
            this.defaultValue = defaultValue;
            return (T) this;
        }

        public T dropDefaultValue() {
            this.hasDefaultValue = false;
            this.defaultValue = null;
            return (T) this;
        }

        public <O> T addResultTranslator(Class<O> originClass, Function<O, R> translator) {
            resultMapper.addMapper(originClass, translator);
            return (T) this;
        }

        public T setResultMapper(ResultMapperCollection<R> resultMapper) {
            this.resultMapper = resultMapper.createChild();
            return (T) this;
        }
    }

    /**
     * Created an instance of Method Singleton Description
     * @param <R> Result type of the method
     */
    public static class Builder<R> extends AbstractBuilder<R, Builder<R>> {
        public Builder(Method wrapperMethod, Class<R> methodResultType) {
            super(wrapperMethod, methodResultType);
        }
    }

    /**
     * Method Singleton
     *
     * @param <W> Class of wrapper
     * @param <R> Class of the method return value
     */
    public static class ShortcutBuilder<W, R>
            extends AbstractBuilder<R, ShortcutBuilder<W, R>> {

        @Delegate
        private final ChildMethodDescriptionBuilderWrapper<W> classFactoryReference;

        public ShortcutBuilder(
                ClassFactory.Builder<W> wrapperClass,
                Method wrapperMethod,
                Class<R> methodResultType) {
            super(wrapperMethod, methodResultType);
            classFactoryReference = new ChildMethodDescriptionBuilderWrapper<>(wrapperClass, this);
        }
    }
}
