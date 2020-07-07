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

package com.github.antkudruk.uniformfactory.base;

import com.github.antkudruk.uniformfactory.base.exception.NoWrapperMethodException;
import com.github.antkudruk.uniformfactory.base.exception.NoMarkerAnnotationException;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.ParameterBindersSource;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialMapper;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialParameterUnion;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.ConstantValue;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.ParameterValue;
import com.github.antkudruk.uniformfactory.singleton.descriptors.ResultMapperCollection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractMethodDescriptorImpl implements MethodDescriptor {

    protected final Class<? extends Annotation> markerAnnotation;
    protected final Method wrapperMethod;
    protected final ResultMapperCollection resultMapper;
    protected final ParameterBindersSource parameterMapper;

    public AbstractMethodDescriptorImpl(BuilderInterface<?, ?> builder) {

        this.markerAnnotation = builder.getMarkerAnnotation();
        this.wrapperMethod = builder.getWrapperMethod();
        this.resultMapper = builder.getResultMapper();
        this.parameterMapper = builder.getParameterMapper();

        validate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Method getWrapperMethod() {
        return wrapperMethod;
    }

    private void validate() {

        if (markerAnnotation == null) {
            throw new NoMarkerAnnotationException();
        }

        if (wrapperMethod == null) {
            throw new NoWrapperMethodException();
        }
    }

    public interface BuilderInterface<M extends Annotation, R> {
        Class<M> getMarkerAnnotation();

        Method getWrapperMethod();

        ResultMapperCollection<R> getResultMapper();

        ParameterBindersSource getParameterMapper();
    }

    public static abstract class Builder<M extends Annotation, R, T
            extends AbstractMethodDescriptorImpl.Builder<M, R, T>>
            implements BuilderInterface<M, R> {

        private final Class<M> markerAnnotation;
        protected final Method wrapperMethod;
        private final List<PartialMapper> parameterMappers = new ArrayList<>();
        private ResultMapperCollection<R> resultMapper;

        public Builder(Class<M> markerAnnotation, Method wrapperMethod, Class<R> methodResultType) {
            this.markerAnnotation = markerAnnotation;
            this.wrapperMethod = wrapperMethod;
            this.resultMapper = new ResultMapperCollection<>(methodResultType);
        }

        @SuppressWarnings("unchecked")
        public <O> T addResultTranslator(Class<O> originClass, Function<O, R> translator) {
            resultMapper.addMapper(originClass, translator);
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T setResultMapper(ResultMapperCollection<R> resultMapper) {
            this.resultMapper = resultMapper.createChild();
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T addParameterTranslator(PartialMapper mapper) {
            parameterMappers.add(mapper);
            return (T) this;
        }

        @Override
        public Class<M> getMarkerAnnotation() {
            return markerAnnotation;
        }

        @Override
        public Method getWrapperMethod() {
            return wrapperMethod;
        }

        @Override
        public ResultMapperCollection<R> getResultMapper() {
            return resultMapper;
        }

        @Override
        public ParameterBindersSource getParameterMapper() {
            return new PartialParameterUnion(parameterMappers.toArray(new PartialMapper[0]));
        }

        public abstract AbstractMethodDescriptorImpl build();
    }

    public static abstract class ShortcutBuilder<M extends Annotation, R, T extends AbstractMethodDescriptorImpl.ShortcutBuilder<M, R, T>>
            extends Builder<M, R, T>
            implements BuilderInterface<M, R> {

        public ShortcutBuilder(Class<M> markerAnnotation, Method wrapperMethod, Class<R> methodResultType) {
            super(markerAnnotation, wrapperMethod, methodResultType);
        }

        @SuppressWarnings("unchecked")
        public <P> ParameterValue.ShortcutBuilder<P, T> parameterSource(Class<P> parameterType, int parameterNumber) {
            return new ParameterValue.ShortcutBuilder<>((T) this, parameterType, parameterNumber);
        }

        @SuppressWarnings("unchecked")
        public <P> ConstantValue.ShortcutBuilder<P, T> constantSource(P constant) {
            return new ConstantValue.ShortcutBuilder<>((T) this, constant);
        }
    }
}