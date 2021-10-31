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

package com.github.antkudruk.uniformfactory.setter.enhanncers;

import com.github.antkudruk.uniformfactory.setter.atomicaccassor.SetterAtomGenerator;
import com.github.antkudruk.uniformfactory.singleton.argument.exceptions.ParameterTranslatorNotFound;
import com.github.antkudruk.uniformfactory.singleton.argument.typemapper.ParameterMappersCollection;
import com.github.antkudruk.uniformfactory.singleton.enhancers.AbstractSingletonEnhancerUsingAtom;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.MethodCall;

import java.lang.reflect.Method;

public class SetterEnhancer extends AbstractSingletonEnhancerUsingAtom {
    public SetterEnhancer(
            String fieldAccessorFieldName,
            TypeDescription originClass,
            FieldDescription originField,
            Method wrapperMethod,
            ParameterMappersCollection<?> parameterMapper) throws ParameterTranslatorNotFound {
        super(
                fieldAccessorFieldName,
                originClass,
                wrapperMethod,
                SetterAtomGenerator.INSTANCE.generateClass(
                        originClass,
                        parameterMapper
                                .findSuitableTranslator(originField.getType().asErasure())
                                .map(ParameterMappersCollection.ParameterMapperDescriptor::getTranslator)
                                .orElseThrow(() -> new ParameterTranslatorNotFound(null)),
                        originField
                ));
    }

    @Override
    protected MethodCall enhanceMethodCall(MethodCall methodCall) {
        return methodCall.withAllArguments();
    }
}
