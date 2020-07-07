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
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.Duplication;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.TypeCreation;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Generates bytecode implementing the following instructios:
 *
 * <pre>
 * {@code
 *     void invoke(OriginType origin) {
 *         this.intermediateWrapperField = new IntermediateWrapperType(origin);
 *     }
 * }
 * </pre>
 *
 * {@code IntermediateWrapperType} should have at least one constructor
 * taking a parameter of the {@code OriginType}
 */
public class InitInnerFieldWithArgumentImplementation extends AbstractImplementation {

    private final String fieldName;
    private final TypeDescription originType;
    private final TypeDescription intermediateWrapperType;

    /**
     * @param fieldName               Name for {@code intermediateWrapperField}
     * @param originType              Origin type - type of the first argument.
     * @param intermediateWrapperType Intermediate type - type of the field.
     */
    public InitInnerFieldWithArgumentImplementation(
            String fieldName,
            TypeDescription originType,
            TypeDescription intermediateWrapperType) {
        this(fieldName, originType, intermediateWrapperType, true);
    }

    private InitInnerFieldWithArgumentImplementation(
            String fieldName,
            TypeDescription originType,
            TypeDescription intermediateWrapperType,
            boolean terminating) {
        super(terminating);
        this.fieldName = fieldName;
        this.originType = originType;
        this.intermediateWrapperType = intermediateWrapperType;
    }

    @Override
    public ByteCodeAppender appender(Target implementationTarget) {
        return new Appender();
    }

    @Override
    protected AbstractTerminatableImplementation cloneNotTerminated() {
        return new InitInnerFieldWithArgumentImplementation(fieldName,
                originType, intermediateWrapperType, false);
    }

    public class Appender implements ByteCodeAppender {

        @Override
        public Size apply(
                MethodVisitor methodVisitor,
                Context implementationContext,
                MethodDescription instrumentedMethod) {

            FieldDescription intermediateWrapperField = TypeDescriptionShortcuts.deepFindRequiredField(
                    instrumentedMethod.getDeclaringType().asErasure(),
                    fieldName);

            if (!intermediateWrapperField.getType().asErasure().isAssignableFrom(intermediateWrapperType)) {
                throw new RuntimeException("Can't assign intermediate wrapper field: wrong wrapper type.");
            }

            return new Size(new StackManipulation.Compound(
                    // Apply method on the field
                    MethodVariableAccess.loadThis(),

                    TypeCreation.of(intermediateWrapperType),
                    Duplication.SINGLE,

                    MethodVariableAccess.REFERENCE.loadFrom(
                            instrumentedMethod.getParameters().get(0).getOffset()
                    ),        // Constructor argument

                    MethodInvocation.invoke(intermediateWrapperType
                            .getDeclaredMethods()
                            .filter(ElementMatchers.isConstructor()
                                    .and(ElementMatchers.takesArguments(originType)))
                            .getOnly()
                    ),

                    FieldAccess.forField(intermediateWrapperField).write(),

                    isTerminating() ? MethodReturn.VOID : StackManipulation.Trivial.INSTANCE
            ).apply(methodVisitor, implementationContext)
                    .getMaximalSize(),
                    instrumentedMethod.getStackSize());

        }
    }
}
