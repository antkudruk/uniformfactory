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

package com.github.antkudruk.uniformfactory.setter.enhanncers;

import com.github.antkudruk.uniformfactory.base.Enhancer;
import com.github.antkudruk.uniformfactory.base.bytecode.EmptyImplementation;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;

import java.lang.reflect.Method;

/**
 * Adds empty implementation of the specified method to the class.
 */
public class DoNothingEnhancer implements Enhancer {
    public final Method wrapperMethod;

    public DoNothingEnhancer(Method wrapperMethod) {
        this.wrapperMethod = wrapperMethod;
    }

    @Override
    public Implementation.Composable addInitiation(Implementation.Composable existingImplementation) {
        return existingImplementation;
    }

    @Override
    public <W> DynamicType.Builder<W> addMethod(DynamicType.Builder<W> bbBuilder) {
        return bbBuilder
                .define(wrapperMethod)
                .intercept(new EmptyImplementation());
    }
}
