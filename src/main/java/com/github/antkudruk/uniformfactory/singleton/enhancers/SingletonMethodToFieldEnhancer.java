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

package com.github.antkudruk.uniformfactory.singleton.enhancers;

import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.field.AccessFieldValue;
import com.github.antkudruk.uniformfactory.singleton.descriptors.ResultMapperCollection;
import com.github.antkudruk.uniformfactory.singleton.descriptors.WrapperMethodTypesException;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.MethodCall;

import java.lang.reflect.Method;

public class SingletonMethodToFieldEnhancer extends AbstractSingletonEnhancerUsingAtom {

    public SingletonMethodToFieldEnhancer(
            String fieldAccessorFieldName,
            TypeDescription originClass,
            FieldDescription originField,
            Method wrapperMethod,
            ResultMapperCollection resultMapper) throws WrapperMethodTypesException {
        super(
                fieldAccessorFieldName,
                originClass,
                wrapperMethod,
                AccessFieldValue.INSTANCE.generateClass(
                        originClass,
                        resultMapper.getTranslatorOrThrow(originField.getType().asErasure()),
                        originField));
    }

    @Override
    protected MethodCall enhanceMethodCall(MethodCall methodCall) {
        return methodCall;
    }
}
