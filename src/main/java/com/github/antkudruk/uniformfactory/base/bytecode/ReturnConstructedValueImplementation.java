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

package com.github.antkudruk.uniformfactory.base.bytecode;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.Duplication;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.TypeCreation;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.matcher.ElementMatchers;

/**
 *
 * {@code
 *     T invoke(Object arg) {
 *         return new T(arg);
 *     }
 * }
 *
 */
// TODO: Add unit test
public class ReturnConstructedValueImplementation extends AbstractImplementation {
    private final TypeDescription typeDescription;
    private final TypeDescription parameterTypeDescription;

    public ReturnConstructedValueImplementation(
            TypeDescription typeDescription,
            TypeDescription parameterTypeDescription) {
        this(typeDescription, parameterTypeDescription, true);
    }

    private ReturnConstructedValueImplementation(
            TypeDescription typeDescription,
            TypeDescription parameterTypeDescription,
            boolean terminate) {
        super(terminate);
        this.typeDescription = typeDescription;
        this.parameterTypeDescription = parameterTypeDescription;
    }

    @Override
    protected AbstractTerminatableImplementation cloneNotTerminated() {
        return new ReturnConstructedValueImplementation(typeDescription, parameterTypeDescription,false);
    }

    @Override
    public ByteCodeAppender appender(Target implementationTarget) {
        return new Appender();
    }

    public class Appender implements ByteCodeAppender {

        @Override
        public Size apply(
                MethodVisitor methodVisitor,
                Context implementationContext,
                MethodDescription instrumentedMethod) {

            if (!instrumentedMethod.getReturnType().asErasure().isAssignableFrom(typeDescription)) {
                throw new RuntimeException("Can't assign intermediate wrapper field: wrong wrapper type.");
            }

            return new Size(new StackManipulation.Compound(
                    // Apply method on the field
                    TypeCreation.of(typeDescription),       // Stack: instance
                    Duplication.SINGLE,                     // Stack: instance, instance
                    MethodVariableAccess.REFERENCE.loadFrom(
                            instrumentedMethod.getParameters().get(0).getOffset()
                    ),                                      // Stack: instance, instance, param
                    TypeCasting.to(parameterTypeDescription),
                    MethodInvocation.invoke(typeDescription
                            .getDeclaredMethods()
                            .filter(ElementMatchers.isConstructor()
                                    .and(ElementMatchers.takesArguments(parameterTypeDescription)))
                            .getOnly()
                    ),                                      // Stack: instance
                    isTerminating() ? MethodReturn.REFERENCE : StackManipulation.Trivial.INSTANCE
            ).apply(methodVisitor, implementationContext)
                    .getMaximalSize(),
                    instrumentedMethod.getStackSize());
        }
    }
}
