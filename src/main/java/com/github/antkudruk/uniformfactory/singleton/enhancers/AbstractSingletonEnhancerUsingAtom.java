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

package com.github.antkudruk.uniformfactory.singleton.enhancers;

import com.github.antkudruk.uniformfactory.base.Enhancer;
import com.github.antkudruk.uniformfactory.base.bytecode.InitInnerFieldWithArgumentImplementation;
import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.Constants;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;

@RequiredArgsConstructor
public abstract class AbstractSingletonEnhancerUsingAtom implements Enhancer {

    private final String fieldAccessorFieldName;
    private final TypeDescription originClass;
    private final Method wrapperMethod;
    private final DynamicType.Unloaded<?> atomClassUnloaded;
    private final TypeDescription atomClass;

    public AbstractSingletonEnhancerUsingAtom(
            String fieldAccessorFieldName,
            TypeDescription originClass,
            Method wrapperMethod,
            DynamicType.Unloaded<?> atomClassUnloaded) {

        this.fieldAccessorFieldName = fieldAccessorFieldName;
        this.originClass = originClass;
        this.wrapperMethod = wrapperMethod;
        this.atomClassUnloaded = atomClassUnloaded;
        this.atomClass = atomClassUnloaded.getTypeDescription();

    }

    @Override
    public Implementation.Composable addInitiation(
            Implementation.Composable existingImplementation) {

        return existingImplementation.andThen(
                new InitInnerFieldWithArgumentImplementation(
                        fieldAccessorFieldName,
                        originClass,
                        atomClass));
    }

    @Override
    public <N> DynamicType.Builder<N> addMethod(DynamicType.Builder<N> bbBuilder) {
        return bbBuilder
                // Define a field
                .defineField(fieldAccessorFieldName, atomClass,
                        Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC)

                // Define a method to access the origin field.
                .define(wrapperMethod)
                .intercept(enhanceMethodCall(
                        (MethodCall)MethodCall
                        .invoke(atomClass
                                .getDeclaredMethods()
                                .filter(ElementMatchers.named(Constants.METHOD_NAME))
                                .getOnly())
                        .onField(fieldAccessorFieldName)
                        .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC)
                )).require(atomClassUnloaded);
    }

    protected abstract MethodCall enhanceMethodCall(MethodCall methodCall);
}
