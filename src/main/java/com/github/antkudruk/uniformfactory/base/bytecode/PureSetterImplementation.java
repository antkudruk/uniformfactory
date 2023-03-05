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

import com.github.antkudruk.uniformfactory.common.TypeDescriptionShortcuts;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.Duplication;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;
import net.bytebuddy.implementation.bytecode.assign.primitive.PrimitiveBoxingDelegate;
import net.bytebuddy.implementation.bytecode.constant.FieldConstant;
import net.bytebuddy.implementation.bytecode.constant.IntegerConstant;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Field;

public class PureSetterImplementation implements Implementation {

    private final String originFieldName;
    private final FieldDescription valueField;

    public PureSetterImplementation (
            String originFieldName,
            FieldDescription valueField) {

        this.originFieldName = originFieldName;
        this.valueField = valueField;
    }

    @Override
    public ByteCodeAppender appender(Implementation.Target implementationTarget) {

        FieldDescription originField = implementationTarget
                .getInstrumentedType()
                .getDeclaredFields()
                .filter(ElementMatchers.named(originFieldName))
                .getOnly();

        return new Appender(originField, valueField);
    }

    @Override
    public InstrumentedType prepare(InstrumentedType instrumentedType) {
        return instrumentedType;
    }

    public static class Appender implements ByteCodeAppender {
        private final FieldDescription originField;
        private final FieldDescription targetField;

        Appender(FieldDescription originField,
                 FieldDescription targetField) {

            this.originField = originField;
            this.targetField = targetField;
        }

        @Override
        public ByteCodeAppender.Size apply(
                MethodVisitor methodVisitor,
                Implementation.Context implementationContext,
                MethodDescription instrumentedMethod) {

            StackManipulation stackManipulation = targetField.isPublic()
                    ? publicFieldStackManipulation(instrumentedMethod)
                    : privateFieldStackManipulation(instrumentedMethod);

            return new Size(stackManipulation.apply(methodVisitor, implementationContext)
                    .getMaximalSize(),
                    instrumentedMethod.getStackSize());
        }

        private StackManipulation privateFieldStackManipulation(
                MethodDescription instrumentedMethod) {
            return new StackManipulation.Compound(
                    new FieldConstant(targetField.asDefined()),
                    // Stack: targetField,
                    Duplication.of(new TypeDescription.ForLoadedType(Field.class)),
                    // stack: targetField, targetField
                    IntegerConstant.forValue(true),
                    // stack: targetField, targetField, 'true'
                    MethodInvocation.invoke(TypeDescriptionShortcuts.deepFindMethod(
                            new TypeDescription.ForLoadedType(Field.class),
                            "setAccessible",
                            boolean.class).orElseThrow(RuntimeException::new)),
                    // stack: targetField
                    MethodVariableAccess.loadThis(),
                    // Stack: targetField, this
                    FieldAccess.forField(originField).read(),
                    // Stack: targetField, origin
                    MethodVariableAccess
                            .of(instrumentedMethod.getParameters().get(0).getType())
                            .loadFrom(instrumentedMethod.getParameters().get(0).getOffset()),
                    // Stack: targetField, origin, value
                    instrumentedMethod.getParameters().get(0).getType().isPrimitive()
                            ? PrimitiveBoxingDelegate
                                    .forPrimitive(instrumentedMethod.getParameters().get(0).getType())
                                    .assignBoxedTo(
                                            instrumentedMethod.getParameters().get(0).getType().asErasure().asBoxed().asGenericType(),
                                            Assigner.DEFAULT,
                                            Assigner.Typing.DYNAMIC
                                    )
                            : StackManipulation.Trivial.INSTANCE,

                    TypeCasting.to(targetField.getType().asErasure().asBoxed()),
                    // Stack: targetField, origin, value
                    MethodInvocation.invoke(TypeDescriptionShortcuts.findMethod(
                            Field.class,
                            "set",
                            Object.class,
                            Object.class).orElseThrow(RuntimeException::new)
                    ),
                    // Stack:
                    MethodReturn.VOID
            );
        }

        private StackManipulation publicFieldStackManipulation(
                MethodDescription instrumentedMethod) {

            return new StackManipulation.Compound(
                    MethodVariableAccess.loadThis(),
                    // Stack: this
                    FieldAccess.forField(originField).read(),
                    // Stack: origin
                    MethodVariableAccess
                            .of(instrumentedMethod.getParameters().get(0).getType())
                            .loadFrom(instrumentedMethod.getParameters().get(0).getOffset()),
                    // Stack: origin, value
                    FieldAccess.forField(targetField).write(),
                    // Stack:
                    MethodReturn.VOID
            );
        }
    }
}
