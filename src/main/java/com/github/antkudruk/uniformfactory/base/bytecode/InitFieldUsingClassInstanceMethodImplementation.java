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
import com.github.antkudruk.uniformfactory.pluginbuilder.MetaClassFactory;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.constant.ClassConstant;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.matcher.ElementMatchers;

/**
 *
 * Generates translator from <b>origin</b> to <b>wrapper</b> object and
 * assigns it to the static field {@code field}.
 *
 * <pre>
 * {@code
 *
 *     static Function <Origin, Wrapper> translator;
 *
 *     void invoke() {
 *          translator = SingletonHolder.INSTANCE.generateMetaClass(this.getClass());
 *     }
 * }
 * </pre>
 */
public class InitFieldUsingClassInstanceMethodImplementation implements Implementation {

    private final TypeDescription singletonHolder;
    private final String fieldName;
    private final Class<? extends MetaClassFactory<?>> classFactoryGenerator;

    public InitFieldUsingClassInstanceMethodImplementation(
            TypeDescription singletonHolder,
            String fieldName,
            Class<? extends MetaClassFactory<?>> classFactoryGenerator) {
        this.singletonHolder = singletonHolder;
        this.fieldName = fieldName;
        this.classFactoryGenerator = classFactoryGenerator;
    }

    @Override
    public ByteCodeAppender appender(Target implementationTarget) {
        return new Appender();
    }

    @Override
    public InstrumentedType prepare(InstrumentedType instrumentedType) {
        return instrumentedType;
    }

    public class Appender implements ByteCodeAppender {

        @Override
        public Size apply(MethodVisitor methodVisitor,
                          Context implementationContext,
                          MethodDescription instrumentedMethod) {

            FieldDescription wrapperInstanceField = TypeDescriptionShortcuts.deepFindStaticField(
                    instrumentedMethod.getDeclaringType().asErasure(),
                    fieldName
            ).orElseThrow(RuntimeException::new);

            return new Size(new StackManipulation.Compound(
                    FieldAccess.forField(singletonHolder.getDeclaredFields()
                            .filter(ElementMatchers.isStatic()
                                    .and(ElementMatchers.named("INSTANCE"))
                                    .and(ElementMatchers.fieldType(classFactoryGenerator))
                            ).getOnly()).read(),
                    // Stack: ConstructorGeneratorInstance,
                    ClassConstant.of(instrumentedMethod.getDeclaringType().asErasure()),
                    // Stack: ConstructorGeneratorInstance, originClass
                    MethodInvocation.invoke(new TypeDescription.ForLoadedType(MetaClassFactory.class)
                            .getDeclaredMethods()
                            .filter(ElementMatchers.named("generateMetaClass")
                                    .and(ElementMatchers.takesArguments(Class.class))
                            )
                            .getOnly()),
                    // Assign tofield
                    FieldAccess.forField(wrapperInstanceField).write(),
                    MethodReturn.VOID
            ).apply(methodVisitor, implementationContext)
                    .getMaximalSize(),
                    instrumentedMethod.getStackSize());
        }
    }
}
