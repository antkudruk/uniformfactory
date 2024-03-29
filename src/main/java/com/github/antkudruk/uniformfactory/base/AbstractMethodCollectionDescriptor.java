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

import lombok.Getter;

import java.lang.reflect.Method;

@Getter
public abstract class AbstractMethodCollectionDescriptor<F> extends AbstractMethodDescriptorImpl {
    private final Class<F> functionalInterface;

    public AbstractMethodCollectionDescriptor(Method wrapperMethod,
                                              Class<F> functionalInterface) {
        super(wrapperMethod);
        this.functionalInterface = functionalInterface;
        validate();
    }

    private void validate() {
        if (functionalInterface == null) {
            throw new RuntimeException("You haven't defined a functional interface");
        }
    }

    @Getter
    public static abstract class AbstractBuilder<F, T extends AbstractBuilder<F, T>>
            extends AbstractMethodDescriptorImpl.AbstractBuilder<T>
            implements Builds<MethodDescriptor> {

        private final Class<F> elementType;
        public AbstractBuilder(Method wrapperMethod, Class<F> elementType) {
            super(wrapperMethod);
            this.elementType = elementType;
        }
    }
}
