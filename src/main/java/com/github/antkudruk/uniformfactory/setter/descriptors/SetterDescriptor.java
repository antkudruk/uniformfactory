/*
    Copyright 2020 - 2021 Anton Kudruk

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

package com.github.antkudruk.uniformfactory.setter.descriptors;

import com.github.antkudruk.uniformfactory.base.Enhancer;
import com.github.antkudruk.uniformfactory.base.MethodDescriptor;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.seletor.MemberSelector;
import com.github.antkudruk.uniformfactory.seletor.MemberSelectorByAnnotation;
import com.github.antkudruk.uniformfactory.setter.enhanncers.DoNothingEnhancer;
import com.github.antkudruk.uniformfactory.setter.enhanncers.SetterEnhancer;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialMapper;
import com.github.antkudruk.uniformfactory.singleton.argument.typemapper.ParameterMappersCollection;
import com.github.antkudruk.uniformfactory.singleton.descriptors.ResultMapperCollection;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.type.TypeDescription;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class SetterDescriptor<A> implements MethodDescriptor {

    private static final String FIELD_NAME_PREFIX = "setterMethod";
    private static final AtomicLong fieldNameIndex = new AtomicLong(0L);

    private final String fieldAccessorFieldName
            = FIELD_NAME_PREFIX + fieldNameIndex.incrementAndGet();

    private final MemberSelector memberSelector;
    private final ParameterMappersCollection<A> parameterMapper;
    private final Method wrapperMethod;

    public SetterDescriptor(BuilderInterface<A> builder) {
        this.memberSelector = builder.getMemberSelector();
        this.wrapperMethod = builder.getWrapperMethod();
        this.parameterMapper = builder.getParameterMapper();

    }

    @Override
    public Method getWrapperMethod() {
        return wrapperMethod;
    }

    @Override
    public Enhancer getEnhancer(TypeDescription originType) throws ClassGeneratorException {

        List<FieldDescription> singletonOriginField = memberSelector.getFields(originType);


        // FieldDescription field = ;
        if(singletonOriginField.size() > 0) {
            return new SetterEnhancer(
                    fieldAccessorFieldName, // singletonOriginField.get(0),
                    originType,
                    singletonOriginField.get(0),
                    wrapperMethod,
                    parameterMapper
            );
        } else {
            // TODO: Test this branch
            return new DoNothingEnhancer(wrapperMethod);
        }
    }

    public interface BuilderInterface<P> {
        MemberSelector getMemberSelector();
        Method getWrapperMethod();
        ParameterMappersCollection<P> getParameterMapper();
        SetterDescriptor<P> build();
    }

    // TODO: Try Lombok to reduce that code nightmare
    public static class Builder<P> implements BuilderInterface<P> {
        private Method wrapperMethod;
        private ParameterMappersCollection<P> parameterMapper;
        private MemberSelector memberSelector;

        public Builder(Method wrapperMethod, Class<P> fieldWrapperType) {
            this.wrapperMethod = wrapperMethod;
            this.parameterMapper = new ParameterMappersCollection<>(fieldWrapperType);
        }

        public SetterDescriptor<P> build() {
            return new SetterDescriptor<>(this);
        }

        @Override
        public MemberSelector getMemberSelector() {
            return memberSelector;
        }

        public Builder<P> setAnnotation(Class<? extends Annotation> annotation) {
             memberSelector = new MemberSelectorByAnnotation(annotation);
             return this;
        }

        public Builder<P> setMemberSelector(MemberSelector memberSelector) {
            this.memberSelector = memberSelector;
            return this;
        }

        @Override
        public Method getWrapperMethod() {
            return wrapperMethod;
        }

        public Builder<P> setWrapperMethod(Method wrapperMethod) {
            this.wrapperMethod = wrapperMethod;
            return this;
        }

        @Override
        public ParameterMappersCollection<P> getParameterMapper() {
            return parameterMapper;
        }

        public Builder<P> setParameterMapper(ParameterMappersCollection<P> parameterMapper) {
            this.parameterMapper = parameterMapper;
            return this;
        }
    }

    public static abstract class IntermediateShortcutBuilder<R, T extends IntermediateShortcutBuilder<R, T>>
            extends Builder<R>
            implements BuilderInterface<R> {

        private MemberSelector memberSelector;
        private final List<PartialMapper> parameterMappers = new ArrayList<>();
        private ResultMapperCollection<R> resultMapper;


        public IntermediateShortcutBuilder(Method wrapperMethod, Class<R> methodResultType) {
            super(wrapperMethod, methodResultType);
        }

        @Override
        public MemberSelector getMemberSelector() {
            return memberSelector;
        }

        public T setMemberSelector(MemberSelector memberSelector) {
            this.memberSelector = memberSelector;
            return (T)this;
        }

        @Override
        public Method getWrapperMethod() {
            return null;
        }

        @Override
        public ParameterMappersCollection getParameterMapper() {
            return null;
        }

        @Override
        public SetterDescriptor<R> build() {
            return new SetterDescriptor<>(this);
        }
    }

    public static final class ShortcutBuilder<R>
            extends IntermediateShortcutBuilder<R, ShortcutBuilder<R>> {

        public ShortcutBuilder(Method wrapperMethod, Class<R> methodResultType) {
            super(wrapperMethod, methodResultType);
        }
    }
}
