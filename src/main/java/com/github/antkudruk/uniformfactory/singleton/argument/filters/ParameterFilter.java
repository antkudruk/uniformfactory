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

package com.github.antkudruk.uniformfactory.singleton.argument.filters;

import net.bytebuddy.description.method.MethodDescription;

/**
 * {@code ParameterFilter} is used to select particular parameter from
 * <b>origin</b> method to pass the value from {@code ValueSource} to.
 */
public interface ParameterFilter {

    /**
     * Decides if the value should be passed to the parameter number {@code originParameterIndex} of the
     * method {@code originMethod}.
     *
     * @param originMethod Origin method receiving the parameter.
     * @param originParameterIndex Ordinal number of the parameter.
     * @return {@code true} if the argument should receive the parameter. Otherwise {@code false}
     */
    boolean useArgument(MethodDescription originMethod, int originParameterIndex);
}
