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

import com.github.antkudruk.uniformfactory.singleton.argument.filters.ParameterFilter;
import net.bytebuddy.description.method.MethodDescription;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Selects <b>origin</b> method parameters by logical <b>and</b>.
 */
public class ParameterFilterConjunction implements ParameterFilter {

    private final List<ParameterFilter> list;

    public ParameterFilterConjunction(ParameterFilter... parameterFilters) {
        this.list = Arrays.stream(parameterFilters).collect(Collectors.toList());
    }

    public ParameterFilterConjunction and(ParameterFilter filter) {
        list.add(filter);
        return this;
    }

    @Override
    public boolean useArgument(MethodDescription method, int originParameterIndex) {
        return list.stream().allMatch(t -> t.useArgument(method, originParameterIndex));
    }
}
