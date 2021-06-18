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

import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.ForStaticField;
import com.github.antkudruk.uniformfactory.common.TypeDescriptionShortcuts;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.Duplication;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.constant.FieldConstant;
import net.bytebuddy.implementation.bytecode.constant.IntegerConstant;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.function.Function;

/**
 * Generates bytecode that returns fields field value, even if the field is private.
 *
 * <pre>
 * {@code
 *      public void invoke() {
 *          field.setAccessible(true);
 *          Object fieldValue = field.get(this.originField);
 *          field.setAccessible(false);
 *          return resultTranslatorLambda.apply(fieldValue);
 *      }
 * }
 * </pre>
 *
 */
public class FieldAccessImplementation implements Implementation {

    private static final String RESULT_TRANSLATOR_FIELD_NAME = "RESULT_TRANSLATOR";

    private final String originFieldName;
    private final FieldDescription valueField;
    private final Function resultTranslator;

    public FieldAccessImplementation(
            String originFieldName,
            FieldDescription valueField,
            Function resultTranslator) {

        this.originFieldName = originFieldName;
        this.valueField = valueField;
        this.resultTranslator = resultTranslator;
    }

    @Override
    public ByteCodeAppender appender(Target implementationTarget) {

        FieldDescription originField = implementationTarget
                .getInstrumentedType()
                .getDeclaredFields()
                .filter(ElementMatchers.named(originFieldName))
                .getOnly();

        return new Appender(originField, valueField);
    }

    @Override
    public InstrumentedType prepare(InstrumentedType instrumentedType) {
        // Initiate result translator
        return instrumentedType
                .withField(new FieldDescription.Token(RESULT_TRANSLATOR_FIELD_NAME,
                        Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE | Opcodes.ACC_SYNTHETIC,
                        new TypeDescription.ForLoadedType(Function.class).asGenericType()))

                .withInitializer(new ForStaticField(RESULT_TRANSLATOR_FIELD_NAME, resultTranslator));
    }

    public static class Appender implements ByteCodeAppender {
        private final FieldDescription originField;
        private final FieldDescription valueField;

        Appender(FieldDescription originField,
                 FieldDescription valueField) {

            this.originField = originField;
            this.valueField = valueField;
        }

        @Override
        public Size apply(
                MethodVisitor methodVisitor,
                Context implementationContext,
                MethodDescription instrumentedMethod) {

            FieldDescription resultTranslatorLambda = TypeDescriptionShortcuts.deepFindRequiredField(
                    instrumentedMethod.getDeclaringType().asErasure(),
                    RESULT_TRANSLATOR_FIELD_NAME);

            return new Size(new StackManipulation.Compound(

                    // Apply method on the field
                    MethodVariableAccess.loadThis(),
                    FieldAccess.forField(resultTranslatorLambda.asDefined()).read(),
                    // Stack: resultTranslatorLambda

                    // TODO: Use public field stack manipulation if it's direct class field, too, \
                    //  'cause it's accessible regardless of modifier
                    valueField.isPublic()
                            ? publicFieldStackManipulation()
                            : privateFieldStackManipulation(),

                    // Apply method on the field
                    MethodInvocation.invoke(TypeDescriptionShortcuts.findMethod(
                            Function.class,
                            "apply",
                            Object.class).orElseThrow(RuntimeException::new)
                    ),

                    MethodReturn.REFERENCE
            ).apply(methodVisitor, implementationContext)
                    .getMaximalSize(),
                    instrumentedMethod.getStackSize());
        }

        /**
         * {@code
         * <p>
         * Object invoke () {
         * valueField.setAccessible(true);
         * result = valueField.get(this.origin);
         * valueField.setAccessible(false);
         * return result;
         * }
         * }
         *
         * @return Stack manipulation to access private field.
         */
        private StackManipulation privateFieldStackManipulation() {
            return new StackManipulation.Compound(
                    // on
                    new FieldConstant(valueField.asDefined()),
                    // stack: valueField
                    Duplication.of(new TypeDescription.ForLoadedType(Field.class)),
                    // stack: valueField, valueField
                    IntegerConstant.forValue(true),
                    // stack: valueField, valueField, 'true'
                    MethodInvocation.invoke(TypeDescriptionShortcuts.deepFindMethod(
                            new TypeDescription.ForLoadedType(Field.class),
                            "setAccessible",
                            boolean.class).orElseThrow(RuntimeException::new)),
                    // Stack: valueField
                    MethodVariableAccess.loadThis(),
                    FieldAccess.forField(originField).read(),
                    // Stack: valueField, origin
                    MethodInvocation.invoke(TypeDescriptionShortcuts.findMethod(
                            Field.class,
                            "get",
                            Object.class).orElseThrow(RuntimeException::new)),
                    // Stack: value

                    // off
                    new FieldConstant(valueField.asDefined()),
                    IntegerConstant.forValue(false),
                    MethodInvocation.invoke(TypeDescriptionShortcuts.findMethod(
                            AccessibleObject.class,
                            "setAccessible",
                            boolean.class).orElseThrow(RuntimeException::new))
            );
        }

        /**
         * {@code
         * <p>
         * Object invoke () {
         * return valueField.get(this.origin);
         * }
         * }
         *
         * @return Stack manipulation to access public field.
         */
        private StackManipulation publicFieldStackManipulation() {
            return new StackManipulation.Compound(
                    new FieldConstant(valueField.asDefined()),
                    // stack: valueField
                    MethodVariableAccess.loadThis(),
                    // stack: valueField, this
                    FieldAccess.forField(originField).read(),
                    // Stack: resultTranslatorLambda, valueField, origin
                    MethodInvocation.invoke(TypeDescriptionShortcuts.findMethod(
                            Field.class,
                            "get",
                            Object.class).orElseThrow(RuntimeException::new))
            );
        }
    }
}