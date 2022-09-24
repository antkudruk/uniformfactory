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

package com.github.antkudruk.uniformfactory.methodmap.enhancers;

import com.github.antkudruk.uniformfactory.base.Enhancer;
import com.github.antkudruk.uniformfactory.base.bytecode.InitMapImplementation;
import lombok.AllArgsConstructor;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.Opcodes;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

/**
 * Enhancer for Method Map.
 * Defines {@code Map} field and initiates it using {@code InitMapImplementation} class.
 * Implements <b>origin</b> method to access that field.
 */
@AllArgsConstructor
public class MethodMapEnhancer<F> implements Enhancer {

    private final String fieldName;
    private final TypeDescription originType;
    private final Method wrapperMethod;
    private final Map<String, DynamicType.Unloaded<F>> functionalMap;

    @Override
    public Implementation.Composable addInitiation(
            Implementation.Composable methodCall) {

        return methodCall.andThen(new InitMapImplementation<F>(
                fieldName,
                originType,
                functionalMap
        ));
    }

    @Override
    public <N> DynamicType.Builder<N> addMethod(DynamicType.Builder<N> bbBuilder) {
        return bbBuilder
                .defineField(fieldName, Map.class, Opcodes.ACC_PRIVATE)
                .define(wrapperMethod)
                .intercept(FieldAccessor.ofField(fieldName))
                .require(new ArrayList<>(functionalMap.values()));
    }
}
