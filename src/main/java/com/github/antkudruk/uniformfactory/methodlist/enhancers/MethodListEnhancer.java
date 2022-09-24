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

package com.github.antkudruk.uniformfactory.methodlist.enhancers;

import com.github.antkudruk.uniformfactory.base.Enhancer;
import com.github.antkudruk.uniformfactory.base.bytecode.InitListImplementation;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.Opcodes;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public class MethodListEnhancer<F> implements Enhancer {

    private final String fieldName;
    private final TypeDescription originType;
    private final Method wrapperMethod;
    private final List<DynamicType.Unloaded<F>> functionalList;

    public MethodListEnhancer(
            String listFieldName,
            TypeDescription originType,
            Method wrapperMethod,
            List<DynamicType.Unloaded<F>> functionalListClasses) {

        this.fieldName = listFieldName;
        this.originType = originType;
        this.wrapperMethod = wrapperMethod;
        this.functionalList = functionalListClasses;
    }

    @Override
    public Implementation.Composable addInitiation(
            Implementation.Composable methodCall) {

        return methodCall.andThen(new InitListImplementation(fieldName,
                originType,
                functionalList.stream()
                        .map(DynamicType::getTypeDescription)
                        .collect(Collectors.toList())));
    }

    @Override
    public <N> DynamicType.Builder<N> addMethod(DynamicType.Builder<N> bbBuilder) {
        return bbBuilder
                .defineField(fieldName, List.class, Opcodes.ACC_PRIVATE)
                .define(wrapperMethod)
                .intercept(FieldAccessor.ofField(fieldName))
                .require(functionalList
                        .stream()
                        .map(e -> (DynamicType)e)
                        .collect(Collectors.toList())
                );
    }
}
