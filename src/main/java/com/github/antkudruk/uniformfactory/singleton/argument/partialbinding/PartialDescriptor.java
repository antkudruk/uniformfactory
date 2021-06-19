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

package com.github.antkudruk.uniformfactory.singleton.argument.partialbinding;

import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodCall;

/**
 * Enhances DynamicType builder to add an auxiliary object that provides
 * the corresponding parameter for the origin method.
 */
public interface PartialDescriptor {

    /**
     *
     * @return Parameter index in the origin method.
     */
    int getOriginIndex();

    /**
     * Adds parameter to the {@code methodCall}.
     * These methods <b>have</b> to be executed <b>in the same order</b> as the corresponding
     * parameters of {@code originMethod} has.
     *
     * @param methodCall to add parameters.
     * @return methodCall for the further processing
     */
    MethodCall addWith(MethodCall methodCall);

    /**
     * Initiates resources for the further mappers (i. e. static fields for
     * persisting argument translators)
     *
     * @param bbBuilder Builder used for building the wrapper class.
     * @param <W> Wrapper class
     * @return updated {@code DynamicType.Builder}
     */
    default <W> DynamicType.Builder<W> initiate(DynamicType.Builder<W> bbBuilder) {
        return bbBuilder;
    }
}
