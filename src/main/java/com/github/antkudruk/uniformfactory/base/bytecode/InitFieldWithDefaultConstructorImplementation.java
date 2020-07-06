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

package com.github.antkudruk.uniformfactory.base.bytecode;

import com.github.antkudruk.uniformfactory.common.TypeDescriptionShortcuts;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.Duplication;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.TypeCreation;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Initiates static field with the name {@code fieldName} using default
 * constructor of the class {@code classFactoryGenerator}.
 *
 * {@code
 *      void invoke() {
 *          field = new ClassFactory();
 *      }
 * }
 */
public class InitFieldWithDefaultConstructorImplementation implements Implementation {

    private final TypeDescription classFactoryGenerator;
    private final String fieldName;

    public InitFieldWithDefaultConstructorImplementation(
            TypeDescription classFactoryGenerator,
            String fieldName) {

        this.classFactoryGenerator = classFactoryGenerator;
        this.fieldName = fieldName;
    }

    @Override
    public ByteCodeAppender appender(Target implementationTarget) {
        return new Appender();
    }

    public class Appender implements ByteCodeAppender {
        @Override
        public ByteCodeAppender.Size apply(
                MethodVisitor methodVisitor,
                Context implementationContext,
                MethodDescription instrumentedMethod) {

            FieldDescription singletonInstanceField = TypeDescriptionShortcuts.deepFindRequiredField(
                    instrumentedMethod.getDeclaringType().asErasure(),
                    fieldName);

            if (!singletonInstanceField.getType().asErasure().isAssignableFrom(classFactoryGenerator)) {
                throw new RuntimeException("Can't assign intermediate wrapper field: wrong wrapper type.");
            }

            return new Size(new StackManipulation.Compound(
                    // Apply method on the field
                    TypeCreation.of(classFactoryGenerator),
                    Duplication.SINGLE,
                    MethodInvocation.invoke(classFactoryGenerator
                            .getDeclaredMethods()
                            .filter(ElementMatchers.isConstructor()
                                    .and(ElementMatchers.takesArguments(0)))
                            .getOnly()
                    ),
                    FieldAccess.forField(singletonInstanceField).write(),
                    MethodReturn.VOID
            ).apply(methodVisitor, implementationContext)
                    .getMaximalSize(),
                    instrumentedMethod.getStackSize());
        }
    }

    @Override
    public InstrumentedType prepare(InstrumentedType instrumentedType) {
        return instrumentedType;
    }
}