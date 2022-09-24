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

import com.github.antkudruk.uniformfactory.methodcollection.seletor.MemberSelector;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.ParameterBindersSource;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.HasParameterTranslator;
import lombok.experimental.Delegate;

import java.lang.reflect.Method;

public abstract class AbstractMethodWithMappersDescriptorImpl extends MethodWithSelectorDescriptor {

    protected final ParameterBindersSource parameterMapper;

    public AbstractMethodWithMappersDescriptorImpl(
            Method wrapperMethod,
            MemberSelector memberSelector,
            ParameterBindersSource parameterMapper
    ) {
        super(wrapperMethod, memberSelector);
        this.parameterMapper = parameterMapper;
    }


    public static abstract class AbstractBuilder<T extends AbstractBuilder<T>>
            extends AbstractMethodDescriptorImpl.AbstractBuilder<T>
            implements HasParameterTranslator {

        @SuppressWarnings("unchecked")
        @Delegate
        private final ParameterMapperBuilder<T> parameterMapperBuilder
                = new ParameterMapperBuilder<>((T) this);

        public AbstractBuilder(Method wrapperMethod) {
            super(wrapperMethod);
        }
    }
}
