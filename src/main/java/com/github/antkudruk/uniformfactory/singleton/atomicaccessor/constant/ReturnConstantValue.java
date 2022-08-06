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

package com.github.antkudruk.uniformfactory.singleton.atomicaccessor.constant;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.jar.asm.Opcodes;

import static com.github.antkudruk.uniformfactory.common.TypeDescriptionShortcuts.findConstructor;
import static com.github.antkudruk.uniformfactory.singleton.atomicaccessor.Constants.METHOD_NAME;
import static com.github.antkudruk.uniformfactory.singleton.atomicaccessor.Constants.ORIGIN_FIELD_NAME;

public class ReturnConstantValue {

    public static final ReturnConstantValue INSTANCE = new ReturnConstantValue();

    public <T> DynamicType.Unloaded generateClass(
            TypeDescription originClass, T value) {
        return new ByteBuddy()
                .subclass(Object.class)

                .defineField(ORIGIN_FIELD_NAME, originClass,
                        Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC)

                .defineConstructor(Visibility.PUBLIC)
                .withParameters(originClass)
                .intercept(MethodCall.invoke(findConstructor(Object.class).orElse(null))
                        .andThen(FieldAccessor.ofField(ORIGIN_FIELD_NAME).setsArgumentAt(0)))
                .defineMethod(METHOD_NAME, Object.class, Visibility.PUBLIC)
                .intercept(value == null ? FixedValue.nullValue() : FixedValue.value(value))
                .make();
    }
}
