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

package com.github.antkudruk.uniformfactory.setter.descriptors;

import com.github.antkudruk.uniformfactory.base.AbstractMethodWithMappersDescriptorImpl;
import com.github.antkudruk.uniformfactory.base.Enhancer;
import com.github.antkudruk.uniformfactory.base.ParameterMapperBuilder;
import com.github.antkudruk.uniformfactory.classfactory.ChildMethodDescriptionBuilderWrapper;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.MemberSelector;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.MemberSelectorByAnnotation;
import com.github.antkudruk.uniformfactory.setter.enhanncers.DoNothingEnhancer;
import com.github.antkudruk.uniformfactory.setter.enhanncers.SetterEnhancer;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.ParameterBindersSource;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.HasParameterTranslator;
import lombok.Getter;
import lombok.experimental.Delegate;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.type.TypeDescription;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class SetterDescriptor<A> extends AbstractMethodWithMappersDescriptorImpl {

    private static final String FIELD_NAME_PREFIX = "setterMethod";
    private static final AtomicLong fieldNameIndex = new AtomicLong(0L);

    private final String fieldAccessorFieldName
            = FIELD_NAME_PREFIX + fieldNameIndex.incrementAndGet();

    public SetterDescriptor(
            Method wrapperMethod,
            MemberSelector memberSelector,
            ParameterBindersSource parameterMapper) {
        super(wrapperMethod, memberSelector, parameterMapper);
    }

    @Override
    public Method getWrapperMethod() {
        return wrapperMethod;
    }

    @Override
    public Enhancer getEnhancer(TypeDescription originType) throws ClassGeneratorException {

        List<FieldDescription> singletonOriginField = memberSelector.getFields(originType);

        if(singletonOriginField.size() > 0) {
            return new SetterEnhancer(
                    fieldAccessorFieldName,
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

    public interface BuilderInterface extends AbstractMethodWithMappersDescriptorImpl.BuilderInterface {
        MemberSelector getMemberSelector();
        Method getWrapperMethod();
        ParameterBindersSource getParameterMapper();
    }

    @SuppressWarnings("unchecked")
    public static abstract class AbstractBuilder<W, T extends AbstractBuilder<W, T>>
            implements BuilderInterface, HasParameterTranslator {

        @Getter
        private Method wrapperMethod;
        @Getter
        private MemberSelector memberSelector;

        @Delegate
        private final ParameterMapperBuilder<T> parameterMapperBuilder = new ParameterMapperBuilder<>((T) this);

        public AbstractBuilder(Method wrapperMethod) {
            this.wrapperMethod = wrapperMethod;
        }

        public SetterDescriptor<W> build() {
            return new SetterDescriptor<>(wrapperMethod, memberSelector, getParameterMapper());
        }

        public T setAnnotation(Class<? extends Annotation> annotation) {
             memberSelector = new MemberSelectorByAnnotation(annotation);
             return (T) this;
        }

        public T setMemberSelector(MemberSelector memberSelector) {
            this.memberSelector = memberSelector;
            return (T) this;
        }

        public T setWrapperMethod(Method wrapperMethod) {
            this.wrapperMethod = wrapperMethod;
            return (T) this;
        }
    }

    public static class Builder<R> extends AbstractBuilder<R, Builder<R>> {

        /**
         *
         * @param wrapperMethod Method to be implemented by the setter
         */
        public Builder(Method wrapperMethod) {
            super(wrapperMethod);
        }
    }

    public static final class ShortcutBuilder<W, R>
            extends AbstractBuilder<R, ShortcutBuilder<W, R>> {

        @Delegate
        private final ChildMethodDescriptionBuilderWrapper<W> classFactoryReference;

        public ShortcutBuilder(
                ClassFactory.Builder<W> parentBuilder,
                Method wrapperMethod) {
            super(wrapperMethod);
            classFactoryReference = new ChildMethodDescriptionBuilderWrapper<>(parentBuilder, this);
        }
    }
}
