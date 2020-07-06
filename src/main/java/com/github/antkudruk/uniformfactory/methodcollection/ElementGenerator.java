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

package com.github.antkudruk.uniformfactory.methodcollection;

import com.github.antkudruk.uniformfactory.base.bytecode.InitInnerFieldWithArgumentImplementation;
import com.github.antkudruk.uniformfactory.common.TypeDescriptionShortcuts;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.function.Function;

public class ElementGenerator {
    public static final ElementGenerator INSTANCE = new ElementGenerator();

    public DynamicType.Unloaded generate(
            TypeDescription originClass,
            TypeDescription elementFunctionalInterface,
            DynamicType.Unloaded intermediateWrapper,
            Function<MethodCall, MethodCall> parametersAppender,
            String intermediateWrapperFieldName) {

        ByteBuddy byteBuddy = new ByteBuddy();

        MethodDescription elementFunction = elementFunctionalInterface.getDeclaredMethods()
                .getOnly();

        MethodDescription intermediateWrapperMethod = intermediateWrapper
                .getTypeDescription()
                .getDeclaredMethods()
                .filter(ElementMatchers
                        .not(ElementMatchers.isConstructor())
                        .and(ElementMatchers.not(ElementMatchers.isStatic())))
                .getOnly();

        return byteBuddy
                .subclass(Object.class,
                        ConstructorStrategy.Default.NO_CONSTRUCTORS)

                .implement(elementFunctionalInterface)

                .defineField(intermediateWrapperFieldName, intermediateWrapper.getTypeDescription(), Opcodes.ACC_PRIVATE)

                .defineConstructor(Visibility.PUBLIC)
                .withParameters(originClass)
                .intercept(MethodCall.invoke(TypeDescriptionShortcuts
                        .findConstructor(Object.class).orElseThrow(RuntimeException::new))
                        .andThen(new InitInnerFieldWithArgumentImplementation(
                                intermediateWrapperFieldName,
                                originClass,
                                intermediateWrapper.getTypeDescription()
                        )))
                .define(elementFunction)
                .intercept(parametersAppender.apply(
                        MethodCall.invoke(intermediateWrapperMethod)
                                .onField(intermediateWrapperFieldName)
                        )
                        .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC)
                )
                .require(intermediateWrapper)
                .make();
    }
}
