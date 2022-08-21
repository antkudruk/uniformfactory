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

import com.github.antkudruk.uniformfactory.base.exception.NoMarkerAnnotationException;
import com.github.antkudruk.uniformfactory.base.exception.NoWrapperMethodException;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.MemberSelector;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.MemberSelectorByAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public abstract class AbstractMethodDescriptorImpl implements MethodDescriptor {

    protected final Method wrapperMethod;
    protected final MemberSelector memberSelector;

    public AbstractMethodDescriptorImpl(BuilderInterface builder) {

        this.memberSelector = builder.getMemberSelector();
        this.wrapperMethod = builder.getWrapperMethod();

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

        if (memberSelector == null) {
            throw new NoMarkerAnnotationException();
        }

        if (wrapperMethod == null) {
            throw new NoWrapperMethodException();
        }
    }

    public interface BuilderInterface {
        MemberSelector getMemberSelector();         // TODO: check to have ONLY ONE if it's singleton
        Method getWrapperMethod();
        MethodDescriptor build();
    }

    /**
     * Base implementation for
     * @param <T>
     */
    public static abstract class AbstractBuilder<T extends AbstractBuilder<T>>
            implements BuilderInterface {

        private MemberSelector memberSelector;
        protected final Method wrapperMethod;

        public AbstractBuilder(Method wrapperMethod) {
            this.wrapperMethod = wrapperMethod;
        }

        @SuppressWarnings("unchecked")
        public <M extends Annotation> T setMarkerAnnotation(Class<M> marker) {
            setMemberSelector(new MemberSelectorByAnnotation(marker));
            return (T)this;
        }

        @SuppressWarnings("unchecked")
        public T setMemberSelector(MemberSelector memberSelector) {
            this.memberSelector = memberSelector;
            return (T)this;
        }

        @Override
        public MemberSelector getMemberSelector() {
            return memberSelector;
        }

        @Override
        public Method getWrapperMethod() {
            return wrapperMethod;
        }

        public abstract AbstractMethodDescriptorImpl build();
    }
}