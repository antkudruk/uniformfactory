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

package com.github.antkudruk.uniformfactory.singleton.atomicaccessor.method;

import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialDescriptor;
import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.CrossLoadersFunctionAdapter;
import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.ForStaticField;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.jar.asm.Opcodes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.function.Function;

import static com.github.antkudruk.uniformfactory.singleton.atomicaccessor.Constants.METHOD_NAME;
import static com.github.antkudruk.uniformfactory.singleton.atomicaccessor.Constants.ORIGIN_FIELD_NAME;

/**
 * Helper class to generate atom object for each described method.
 */
public class AccessMethodInvocation {

    public static final AccessMethodInvocation INSTANCE = new AccessMethodInvocation();

    private static final String RESULT_TRANSLATOR = "RESULT_TRANSLATOR";

    /**
     * @param originClass        Origin class
     * @param resultTranslator   Translate origin result to wrapper result.
     * @param originMethod       Origin method to call.
     * @param wrapperMethod      Wrapper method to call the origin method from.
     * @param partialDescriptors Descriptor for each method parameter
     * @return Class for the wrapper field.
     */
    @SuppressWarnings("unchecked")
    public DynamicType.Unloaded generateClass(
            TypeDescription originClass,
            Function resultTranslator,
            MethodDescription originMethod,
            Method wrapperMethod,
            List<PartialDescriptor> partialDescriptors) {

        DynamicType.Builder bbBuilder = new ByteBuddy()
                .subclass(Object.class, ConstructorStrategy.Default.NO_CONSTRUCTORS);

        bbBuilder = createCunstructorSettingUpOrigin(bbBuilder, originClass);

        bbBuilder = createResultTranslatorField(bbBuilder, resultTranslator);

        for (PartialDescriptor it : partialDescriptors) {
            bbBuilder = it.initiate(bbBuilder);
        }

        bbBuilder = bbBuilder
                .defineMethod(METHOD_NAME, wrapperMethod.getReturnType(), Visibility.PUBLIC)
                .withParameters(getParameterClasses(wrapperMethod.getParameters()))
                .intercept(addResultTranslator(createMethodCall(originMethod, partialDescriptors)));

        return bbBuilder.make();
    }

    /**
     * Generates method call to intercept origin method call taking in consideration
     * parameter translators.
     *
     * @param originMethod Origin method to invoke
     * @return Method call to invoke origin method with its parameter translators.
     */
    private MethodCall createMethodCall(
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

    private DynamicType.Builder createResultTranslatorField(
            DynamicType.Builder bbBuilder,
            Function translator) {

        return bbBuilder.defineField(RESULT_TRANSLATOR, CrossLoadersFunctionAdapter.class,
                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC)
                .initializer(new ForStaticField(RESULT_TRANSLATOR,
                        new CrossLoadersFunctionAdapter(translator)));
    }

    private DynamicType.Builder createCunstructorSettingUpOrigin(
            DynamicType.Builder bbBuilder, TypeDescription originClass) {
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

    private static Class[] getParameterClasses(Parameter[] parameters) {
        Class[] parameterClasses = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterClasses[i] = parameters[i].getType();
        }
        return parameterClasses;
    }

    private MethodCall addResultTranslator(MethodCall methodCall) {
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
