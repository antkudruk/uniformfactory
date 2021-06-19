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

package com.github.antkudruk.uniformfactory.singleton.enhancers;

import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.constant.ReturnConstantValue;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.MethodCall;

import java.lang.reflect.Method;

public class SingletonMethodToConstantEnhancer<C> extends AbstractSingletonEnhancerUsingAtom {

    public SingletonMethodToConstantEnhancer(
            String fieldAccessorFieldName,
            TypeDescription originClass,
            C constant,
            Method wrapperMethod) {

        super(
                fieldAccessorFieldName,
                originClass,
                wrapperMethod,
                ReturnConstantValue.INSTANCE.generateClass(originClass, constant));
    }

    @Override
    protected MethodCall enhanceMethodCall(MethodCall methodCall) {
        return methodCall;
    }
}
