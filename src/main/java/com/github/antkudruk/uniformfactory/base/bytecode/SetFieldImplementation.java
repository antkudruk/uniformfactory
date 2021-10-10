package com.github.antkudruk.uniformfactory.base.bytecode;

import com.github.antkudruk.uniformfactory.common.TypeDescriptionShortcuts;
import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.ForStaticField;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.Duplication;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;
import net.bytebuddy.implementation.bytecode.constant.FieldConstant;
import net.bytebuddy.implementation.bytecode.constant.IntegerConstant;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Field;
import java.util.function.Function;

public class SetFieldImplementation implements Implementation {

    private static final String RESULT_TRANSLATOR_FIELD_NAME = "RESULT_TRANSLATOR";

    private final String originFieldName;
    private final FieldDescription valueField;
    private final Function resultTranslator;

    public SetFieldImplementation (
            String originFieldName,
            FieldDescription valueField,
            Function resultTranslator) {

        this.originFieldName = originFieldName;
        this.valueField = valueField;
        this.resultTranslator = resultTranslator;
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
        // Initiate result translator
        return instrumentedType
                .withField(new FieldDescription.Token(RESULT_TRANSLATOR_FIELD_NAME,
                        Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE | Opcodes.ACC_SYNTHETIC,
                        new TypeDescription.ForLoadedType(Function.class).asGenericType()))

                .withInitializer(new ForStaticField(RESULT_TRANSLATOR_FIELD_NAME, resultTranslator));
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

            FieldDescription resultTranslatorLambda = TypeDescriptionShortcuts.deepFindRequiredField(
                    instrumentedMethod.getDeclaringType().asErasure(),
                    RESULT_TRANSLATOR_FIELD_NAME);

            StackManipulation stackManipulation = targetField.isPublic()
                ? publicFieldStackManipulation(instrumentedMethod, resultTranslatorLambda)
                : privateFieldStackManipulation(instrumentedMethod, resultTranslatorLambda);

            return new Size(stackManipulation.apply(methodVisitor, implementationContext)
                    .getMaximalSize(),
                    instrumentedMethod.getStackSize());
        }

        private StackManipulation privateFieldStackManipulation(
                MethodDescription instrumentedMethod,
                FieldDescription resultTranslatorLambda) {
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
                    FieldAccess.forField(resultTranslatorLambda.asDefined()).read(),
                    // Stack: targetField, origin, translator
                    MethodVariableAccess.REFERENCE.loadFrom(
                            instrumentedMethod.getParameters().get(0).getOffset()),
                    // Stack: targetField, origin, translator, argument
                    MethodInvocation.invoke(TypeDescriptionShortcuts.findMethod(
                            Function.class,
                            "apply",
                            Object.class).orElseThrow(RuntimeException::new)
                    ),
                    // Stack: targetField, origin, value
                    TypeCasting.to(targetField.getType().asErasure()), // TODO: Can get rid?
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
                MethodDescription instrumentedMethod,
                FieldDescription resultTranslatorLambda) {

            return new StackManipulation.Compound(
                    MethodVariableAccess.loadThis(),
                    // Stack: this
                    FieldAccess.forField(originField).read(),
                    // Stack: origin
                    FieldAccess.forField(resultTranslatorLambda.asDefined()).read(),
                    // Stack: origin, translator
                    MethodVariableAccess.REFERENCE.loadFrom(
                            instrumentedMethod.getParameters().get(0).getOffset()),
                    // Stack: origin, translator, argument
                    MethodInvocation.invoke(TypeDescriptionShortcuts.findMethod(
                            Function.class,
                            "apply",
                            Object.class).orElseThrow(RuntimeException::new)
                    ),
                    // Stack: origin, value
                    TypeCasting.to(targetField.getType().asErasure()), // TODO: Can get rid?
                    // Stack: origin, value
                    FieldAccess.forField(targetField).write(),
                    // Stack:
                    MethodReturn.VOID
            );
        }
    }
}
