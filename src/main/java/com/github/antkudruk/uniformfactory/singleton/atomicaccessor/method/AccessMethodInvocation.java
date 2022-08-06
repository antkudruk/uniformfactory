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

package com.github.antkudruk.uniformfactory.singleton.atomicaccessor.method;

import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialDescriptor;
import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.AbstractAtomGenerator;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

import static com.github.antkudruk.uniformfactory.singleton.atomicaccessor.Constants.METHOD_NAME;

/**
 * Helper class to generate atom object for each described method.
 */
public class AccessMethodInvocation extends AbstractAtomGenerator {

    public static final AccessMethodInvocation INSTANCE = new AccessMethodInvocation();


    /**
     *
     * Partial descriptors are passed in the order corresponding to the method
     * signature.
     *
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
}
