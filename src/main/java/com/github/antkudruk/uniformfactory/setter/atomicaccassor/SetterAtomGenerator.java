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

package com.github.antkudruk.uniformfactory.setter.atomicaccassor;

import com.github.antkudruk.uniformfactory.base.bytecode.InitInnerFieldWithArgumentImplementation;
import com.github.antkudruk.uniformfactory.base.bytecode.PureSetterImplementation;
import com.github.antkudruk.uniformfactory.singleton.argument.exceptions.ParameterTranslatorNotFound;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.ParameterBindersSource;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialDescriptor;
import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.AbstractAtomGenerator;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.List;

import static com.github.antkudruk.uniformfactory.common.TypeDescriptionShortcuts.findConstructor;
import static com.github.antkudruk.uniformfactory.singleton.atomicaccessor.Constants.METHOD_NAME;
import static com.github.antkudruk.uniformfactory.singleton.atomicaccessor.Constants.ORIGIN_FIELD_NAME;

public class SetterAtomGenerator extends AbstractAtomGenerator {
    public static final SetterAtomGenerator INSTANCE = new SetterAtomGenerator();
    public static final String SET_METHOD_NAME = "set";

    public DynamicType.Unloaded generateClass(
            TypeDescription originClass,
            Method wrapperMethod,
            ParameterBindersSource parameterMapper,
            FieldDescription fieldType) throws ParameterTranslatorNotFound {

        DynamicType.Unloaded pureSetterClass = getTypeWithPureSetter(originClass, fieldType);
        MethodDescription pureSetterMethod = pureSetterClass
                .getTypeDescription()
                .getDeclaredMethods()
                .filter(ElementMatchers.named(SET_METHOD_NAME))
                .getOnly();

        List<PartialDescriptor> partialDescriptors = parameterMapper.getParameterBinders(pureSetterMethod);

        DynamicType.Builder bbBuilder = new ByteBuddy()
                .subclass(Object.class, ConstructorStrategy.Default.NO_CONSTRUCTORS);
        bbBuilder = bbBuilder
                .defineField(ORIGIN_FIELD_NAME, pureSetterClass.getTypeDescription(),
                Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC)
                .defineConstructor(Visibility.PUBLIC)
                .withParameters(originClass)
                .intercept(MethodCall.invoke(findConstructor(Object.class).orElse(null))
                        .andThen(new InitInnerFieldWithArgumentImplementation(
                                ORIGIN_FIELD_NAME,
                                originClass,
                                pureSetterClass.getTypeDescription()
                        )))
                .require(pureSetterClass);

        for (PartialDescriptor it : partialDescriptors) {
            bbBuilder = it.initiate(bbBuilder);
        }

        bbBuilder = bbBuilder
                .defineMethod(METHOD_NAME, void.class, Visibility.PUBLIC)
                .withParameters(getParameterClasses(wrapperMethod.getParameters()))
                .intercept(createMethodCall(pureSetterMethod, partialDescriptors));

        return bbBuilder.make();
    }

    private DynamicType.Unloaded getTypeWithPureSetter(
            TypeDescription originClass,
            FieldDescription fieldDescription
    ) {
        return new ByteBuddy()
                .subclass(Object.class)
                .defineField(ORIGIN_FIELD_NAME, originClass,
                        Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC)
                .defineConstructor(Visibility.PUBLIC)
                .withParameters(originClass)
                .intercept(MethodCall.invoke(findConstructor(Object.class).orElse(null))
                        .andThen(FieldAccessor.ofField(ORIGIN_FIELD_NAME).setsArgumentAt(0)))
                // Pure setter without any translation
                .defineMethod(SET_METHOD_NAME, void.class, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC)
                .withParameters(fieldDescription.getType())
                .intercept(new PureSetterImplementation(ORIGIN_FIELD_NAME, fieldDescription))
                .make();
    }
}
