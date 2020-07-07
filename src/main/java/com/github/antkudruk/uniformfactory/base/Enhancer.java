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

package com.github.antkudruk.uniformfactory.base;

import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;

/**
 * Adds functionality related to a particular wrapper method to a wrapper class.
 *
 * Enhancer implementations are generated for each origin class and therefore
 * aware of exact origin class members they operate with.
 */
public interface Enhancer {

    /**
     * Enhances wrapper class constructor.
     * The method normally adds initiation on fields accessible by the method.
     *
     * @param existingImplementation Builder for wrapper constructor implementation.
     * @return Constructor implementation enhanced with the method related initiation.
     */
    Implementation.Composable addInitiation(
            Implementation.Composable existingImplementation);

    /**
     * Adds class members required for the method.
     * That members include the method itself and may include adding some
     * fields accessed by the method.
     *
     * @param bbBuilder Builder for wrapper class description
     * @param <W> Wrapper class
     * @return Wrapper class builder enhanced with the wrapper method.
     */
    <W> DynamicType.Builder<W> addMethod(DynamicType.Builder<W> bbBuilder);
}
