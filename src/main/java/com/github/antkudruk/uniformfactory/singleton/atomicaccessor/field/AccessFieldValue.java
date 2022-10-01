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

package com.github.antkudruk.uniformfactory.singleton.atomicaccessor.field;

import com.github.antkudruk.uniformfactory.base.bytecode.FieldAccessImplementation;
import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.AbstractAtomGenerator;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;

import java.lang.reflect.Method;
import java.util.function.Function;

import static com.github.antkudruk.uniformfactory.singleton.atomicaccessor.Constants.METHOD_NAME;
import static com.github.antkudruk.uniformfactory.singleton.atomicaccessor.Constants.ORIGIN_FIELD_NAME;

public class AccessFieldValue extends AbstractAtomGenerator {

    public static final AccessFieldValue INSTANCE = new AccessFieldValue();

    public DynamicType.Unloaded generateClass(
            TypeDescription originClass,
            Function resultTranslator,
            FieldDescription fieldDescription,
            Method wrapperMethod) {


        DynamicType.Builder bbBuilder = new ByteBuddy()
                .subclass(Object.class, ConstructorStrategy.Default.NO_CONSTRUCTORS);

        bbBuilder = createConstructorSettingUpOrigin(bbBuilder, originClass);
        //bbBuilder = createResultTranslatorField(bbBuilder, resultTranslator);

        bbBuilder = bbBuilder
                .defineMethod(METHOD_NAME, wrapperMethod.getReturnType(), Visibility.PUBLIC)
                .intercept(new FieldAccessImplementation(
                        ORIGIN_FIELD_NAME,
                        fieldDescription,
                        resultTranslator
                ));

        return bbBuilder.make();
    }
}
