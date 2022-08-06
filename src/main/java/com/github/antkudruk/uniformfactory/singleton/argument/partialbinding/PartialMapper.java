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

package com.github.antkudruk.uniformfactory.singleton.argument.partialbinding;

import net.bytebuddy.description.method.MethodDescription;

import java.util.List;

public interface PartialMapper {

    /**
     * Generates list of Partial Descriptors for particular method.
     * Instance of {@code PartialMapper} is grouped into
     * {@link PartialParameterUnion}
     * and passed to MethodDescriptor to describe which values have to be
     * passed into origin method as parameters.
     *
     * @param originMethod Method to get argument binders
     * @return List of argumant binders for the method {@code originMethod}
     * @see PartialDescriptor
     */
    List<PartialDescriptor> getArgumentBinders(MethodDescription originMethod);
}
