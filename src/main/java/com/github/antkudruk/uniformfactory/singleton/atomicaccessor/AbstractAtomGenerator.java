/*
    Copyright 2020 - 2022 Anton Kudruk

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

package com.github.antkudruk.uniformfactory.singleton.atomicaccessor;

import com.github.antkudruk.uniformfactory.base.TypeShortcuts;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialDescriptor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.jar.asm.Opcodes;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static com.github.antkudruk.uniformfactory.singleton.atomicaccessor.Constants.ORIGIN_FIELD_NAME;

public abstract class AbstractAtomGenerator {

    private static final String RESULT_TRANSLATOR = "RESULT_TRANSLATOR";

    /**
     * Generates method call to intercept origin method call taking in consideration
     * parameter translators.
     *
     * Parameter translators are taken from {@code partialDescriptors}
     *
     * @param originMethod Origin method to invoke.
     * @param partialDescriptors Collection of descriptors to map method parameters.
     * @return Method call to invoke origin method with its parameter translators.
     */
    protected static MethodCall createMethodCall(
            MethodDescription originMethod,
            List<PartialDescriptor> partialDescriptors) {

        MethodCall methodCall = MethodCall
                .invoke(originMethod)
                .onField(ORIGIN_FIELD_NAME);

        for (PartialDescriptor it : partialDescriptors) {
            methodCall = it.addWith(methodCall);
        }

        return (MethodCall) methodCall.withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC);
    }

    protected static <B> DynamicType.Builder<B> createResultTranslatorField(
            DynamicType.Builder<B> bbBuilder,
            Function translator) {

        return bbBuilder.defineField(RESULT_TRANSLATOR, CrossLoadersFunctionAdapter.class,
                        Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC)
                .initializer(new ForStaticField(RESULT_TRANSLATOR,
                        new CrossLoadersFunctionAdapter(translator)));
    }

    protected static <B> DynamicType.Builder<B> createConstructorSettingUpOrigin(
            DynamicType.Builder<B> bbBuilder, TypeDescription originClass) {
        try {
            return bbBuilder.defineField(ORIGIN_FIELD_NAME, originClass,
                            Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC)
                    .defineConstructor(Visibility.PUBLIC)
                    .withParameters(originClass)
                    .intercept(MethodCall
                            .invoke(Object.class.getConstructor())
                            .andThen(FieldAccessor.ofField(ORIGIN_FIELD_NAME).setsArgumentAt(0)));
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected static Class<?>[] getParameterClasses(Parameter[] parameters) {
        return Arrays.stream(parameters)
                .map(Parameter::getType)
                .map(TypeShortcuts::getBoxedType)
                .toArray(Class[]::new);
    }

    protected static MethodCall addResultTranslator(MethodCall methodCall) {
        try {
            return (MethodCall) MethodCall
                    .invoke(CrossLoadersFunctionAdapter.class.getMethod("apply", Object.class))
                    .onField(RESULT_TRANSLATOR)
                    .withMethodCall(methodCall)
                    .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }
}
