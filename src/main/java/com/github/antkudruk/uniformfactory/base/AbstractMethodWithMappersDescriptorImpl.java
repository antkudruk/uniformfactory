/*
    Copyright 2020 - Present Anton Kudruk

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

import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.ParameterBindersSource;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialMapper;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialParameterUnion;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.ConstantValue;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.ParameterValue;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractMethodWithMappersDescriptorImpl extends AbstractMethodDescriptorImpl {

    protected final ParameterBindersSource parameterMapper;

    public AbstractMethodWithMappersDescriptorImpl(BuilderInterface builder) {
        super(builder);
        this.parameterMapper = builder.getParameterMapper();
    }

    public interface BuilderInterface extends AbstractMethodDescriptorImpl.BuilderInterface {
        ParameterBindersSource getParameterMapper();
    }

    @SuppressWarnings("unchecked")
    public static abstract class AbstractBuilder<T extends AbstractBuilder<T>>
            extends AbstractMethodDescriptorImpl.AbstractBuilder<T>
            implements BuilderInterface {

        private final List<PartialMapper> parameterMapper = new ArrayList<>();
        @Getter
        private ParameterBindersSource partialParameterUnion = new PartialParameterUnion(Collections.emptyList());

        public AbstractBuilder(Method wrapperMethod) {
            super(wrapperMethod);
        }

        public T setParameterMapper(ParameterBindersSource partialParameterUnion) {
            this.partialParameterUnion = partialParameterUnion;
            return (T) this;
        }

        public T addParameterTranslator(PartialMapper mapper) {
            parameterMapper.add(mapper);
            partialParameterUnion = partialParameterUnion.add(mapper);
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public <P> ParameterValue.ShortcutBuilder<P, T> parameterSource(Class<P> parameterType, int parameterNumber) {
            return new ParameterValue.ShortcutBuilder<>((T) this, parameterType, parameterNumber);
        }

        @SuppressWarnings("unchecked")
        public <P> ConstantValue.ShortcutBuilder<P, T> constantSource(P constant) {
            return new ConstantValue.ShortcutBuilder<>((T) this, constant);
        }

        @Override
        public ParameterBindersSource getParameterMapper() {
            return this.partialParameterUnion;
        }
    }
}
