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

import com.github.antkudruk.uniformfactory.base.exception.NoWrapperMethodException;

import java.lang.reflect.Method;

public abstract class AbstractMethodDescriptorImpl implements MethodDescriptor {

    protected final Method wrapperMethod;

    public AbstractMethodDescriptorImpl(Method wrapperMethod) {
        this.wrapperMethod = wrapperMethod;

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
        if (wrapperMethod == null) {
            throw new NoWrapperMethodException();
        }
    }

    /**
     * Base implementation for
     * @param <T> Builder subclass
     */
    public static abstract class AbstractBuilder<T extends AbstractBuilder<T>>
    implements Builds<MethodDescriptor> {

        protected final Method wrapperMethod;

        public AbstractBuilder(Method wrapperMethod) {
            this.wrapperMethod = wrapperMethod;
        }

        public abstract AbstractMethodDescriptorImpl build();
    }
}