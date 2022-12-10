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

package com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes;

import com.github.antkudruk.uniformfactory.base.TypeShortcuts;
import com.github.antkudruk.uniformfactory.singleton.argument.filters.ParameterFilter;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.description.method.MethodDescription;

/**
 * Selects <b>origin</b> method parameters with {@code parameterClass} type.
 */
@RequiredArgsConstructor
public class ParameterTypeFilter<O> implements ParameterFilter {

    private final Class<O> parameterClass;

    @Override
    public boolean useArgument(MethodDescription method, int originParameterIndex) {
        return method
                .getParameters()
                .get(originParameterIndex)
                .getType()
                .asErasure()
                .asBoxed()
                .represents(TypeShortcuts.getBoxedType(parameterClass));
    }
}
