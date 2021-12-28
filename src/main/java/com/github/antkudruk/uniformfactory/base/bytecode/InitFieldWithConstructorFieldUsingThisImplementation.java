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

package com.github.antkudruk.uniformfactory.base.bytecode;

import com.github.antkudruk.uniformfactory.common.TypeDescriptionShortcuts;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;
import net.bytebuddy.implementation.bytecode.assign.primitive.PrimitiveUnboxingDelegate;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.function.Function;

/**
 *
 * Generates bytecode for the following statement
 *
 * <pre>
 * {@code
 *      public void intercept() {
 *          wrapperField = wrapperConstructorField.invoke(new Object[]{this});
 *      }
 * }
 * </pre>
 */
public class InitFieldWithConstructorFieldUsingThisImplementation extends AbstractImplementation {

    private final String originToWrapperGeneratorFieldName;
    private final String wrapperFieldName;

    /**
     * @param originToWrapperGeneratorFieldName Name for the field containing constructor.
     * @param wrapperFieldName Name for the field to initiate wrapper.
     */
    public InitFieldWithConstructorFieldUsingThisImplementation(
            String originToWrapperGeneratorFieldName,
            String wrapperFieldName) {
        this(originToWrapperGeneratorFieldName, wrapperFieldName, true);
    }

    private InitFieldWithConstructorFieldUsingThisImplementation(
            String originToWrapperGeneratorFieldName,
            String wrapperFieldName,
            boolean terminate) {
        super(terminate);
        this.originToWrapperGeneratorFieldName = originToWrapperGeneratorFieldName;
        this.wrapperFieldName = wrapperFieldName;
    }

    @Override
    protected AbstractTerminatableImplementation cloneNotTerminated() {
        return new InitFieldWithConstructorFieldUsingThisImplementation(
                originToWrapperGeneratorFieldName,
                wrapperFieldName,
                false);
    }

    @Override
    public ByteCodeAppender appender(Target implementationTarget) {
        return (methodVisitor, implementationContext, instrumentedMethod) -> {
            FieldDescription wrapperConstructorField
                    = TypeDescriptionShortcuts.deepFindStaticField(
                    instrumentedMethod.getDeclaringType().asErasure(),
                    originToWrapperGeneratorFieldName).orElseThrow(RuntimeException::new);

            FieldDescription wrapperField
                    = TypeDescriptionShortcuts.findField(
                    instrumentedMethod.getDeclaringType().asErasure(),
                    wrapperFieldName).orElseThrow(RuntimeException::new);

            return new ByteCodeAppender.Size(new StackManipulation.Compound(
                    MethodVariableAccess.loadThis(),
                    FieldAccess.forField(wrapperConstructorField).read(),
                    MethodVariableAccess.loadThis(),
                    MethodInvocation.invoke(
                            new TypeDescription
                                    .ForLoadedType(Function.class)
                                    .getDeclaredMethods()
                                    .filter(ElementMatchers.named("apply"))
                                    .getOnly()),
                    TypeCasting.to(wrapperField.getType()), // TODO: Can get rid?

                    // TODO: Test
                    wrapperField.getType().isPrimitive()
                            ? PrimitiveUnboxingDelegate.forPrimitive(wrapperField.getType())
                            : StackManipulation.Trivial.INSTANCE,

                    FieldAccess.forField(wrapperField).write(),
                    isTerminating() ? MethodReturn.VOID : StackManipulation.Trivial.INSTANCE
            ).apply(methodVisitor, implementationContext)
                    .getMaximalSize(),
                    instrumentedMethod.getStackSize());
        };
    }
}