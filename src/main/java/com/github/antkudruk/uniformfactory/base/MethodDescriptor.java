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

package com.github.antkudruk.uniformfactory.base;

import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import net.bytebuddy.description.type.TypeDescription;

import java.lang.reflect.Method;

/**
 * Describes how to implement each wrapper method.
 */
public interface MethodDescriptor {

    /**
     *
     * @return Wrapper method described by this descriptor.
     */
    Method getWrapperMethod();

    /**
     * Explores {@code originType} and returns the appropriate method
     * enhancer that provides access to the needed class members.
     *
     * @param originType Type of origin class to generate wrapper for.
     * @return Enhancer for a specified {@code originType}
     * @throws ClassGeneratorException Thrown if the {@code originClass} \
     *                          doesn't meet the enhancer requirements.
     */
    Enhancer getEnhancer(TypeDescription originType)
            throws ClassGeneratorException;
}
