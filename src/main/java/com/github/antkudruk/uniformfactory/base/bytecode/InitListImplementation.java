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
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.Duplication;
import net.bytebuddy.implementation.bytecode.Removal;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.TypeCreation;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * Generates bytecode for the following instructions.
 *
 * <pre>
 * {@code
 *      void intercept(OriginType origin) {
 *          List temp = new ArrayList();
 *
 *          temp.add(new Element0(origin));
 *          temp.add(new Element1(origin));
 *          // ... ... ...
 *          temp.add(new ElementN(origin));
 *
 *          this.field = Collections.unmodifiableList(temp);
 *      }
 * }
 * </pre>
 *
 */
public class InitListImplementation extends AbstractImplementation {

    private final String fieldName;
    private final TypeDescription originType;
    private final List<TypeDescription> functionalObjects;

    public InitListImplementation(
            String fieldName,
            TypeDescription originType,
            List<TypeDescription> functionalObjects) {

        this(fieldName, originType, functionalObjects, true);
    }

    private InitListImplementation(
            String fieldName,
            TypeDescription originType,
            List<TypeDescription> functionalObjects,
            boolean isTerminating) {
        super(isTerminating);
        this.fieldName = fieldName;
        this.originType = originType;
        this.functionalObjects = functionalObjects;
    }

    @Override
    public ByteCodeAppender appender(Target implementationTarget) {
        return new Appender();
    }

    @Override
    protected AbstractTerminatableImplementation cloneNotTerminated() {
        return new InitListImplementation(fieldName, originType, functionalObjects, false);
    }

    public class Appender implements ByteCodeAppender {

        @Override
        public Size apply(
                MethodVisitor methodVisitor,
                Context implementationContext,
                MethodDescription instrumentedMethod) {

            FieldDescription intermediateWrapperField = TypeDescriptionShortcuts.deepFindRequiredField(
                    instrumentedMethod.getDeclaringType().asErasure(), fieldName);

            if (!intermediateWrapperField.getType().asErasure().isAssignableFrom(List.class)) {
                throw new RuntimeException("Can't assign intermediate wrapper field: wrong wrapper type.");
            }

            List<StackManipulation> operands = new ArrayList<>(Arrays.asList(
                    MethodVariableAccess.loadThis(),        // Used by set field instruction
                    TypeCreation.of(new TypeDescription.ForLoadedType(ArrayList.class)),
                    Duplication.SINGLE,
                    MethodInvocation.invoke(TypeDescriptionShortcuts
                            .findConstructor(ArrayList.class).orElseThrow(RuntimeException::new)
                    )
            ));

            for (TypeDescription entry : functionalObjects) {
                operands.addAll(getEachElementInstructions(
                        entry,
                        instrumentedMethod));
            }

            operands.add(MethodInvocation.invoke(
                    TypeDescriptionShortcuts.findStaticMethod(
                            Collections.class, List.class,
                            "unmodifiableList", List.class).orElseThrow(RuntimeException::new)));

            operands.add(FieldAccess.forField(intermediateWrapperField).write());

            if (InitListImplementation.this.isTerminating()) {
                operands.add(MethodReturn.VOID);
            }

            return new Size(new StackManipulation.Compound(
                    operands
            ).apply(methodVisitor, implementationContext)
                    .getMaximalSize(),
                    instrumentedMethod.getStackSize());
        }

        // Adds value supposing the top of the stack points to the ArrayList<>()
        private List<StackManipulation> getEachElementInstructions(
                TypeDescription partialValueType,
                MethodDescription instrumentedMethod) {

            return Arrays.asList(
                    Duplication.SINGLE,
                    TypeCreation.of(partialValueType),
                    Duplication.SINGLE,

                    MethodVariableAccess.REFERENCE.loadFrom(
                            instrumentedMethod.getParameters().get(0).getOffset()),        // Constructor argument,

                    MethodInvocation.invoke(partialValueType
                            .getDeclaredMethods()
                            .filter(ElementMatchers.isConstructor()
                                    .and(ElementMatchers.takesArguments(originType))
                            )
                            .getOnly()
                    ),
                    MethodInvocation.invoke(TypeDescriptionShortcuts.findMethod(
                            ArrayList.class, "add", Object.class).orElseThrow(RuntimeException::new)),
                    Removal.SINGLE
            );
        }
    }
}
