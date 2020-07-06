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

package com.github.antkudruk.uniformfactory.singleton.argument.partialbinding;

import com.github.antkudruk.uniformfactory.singleton.argument.filters.ParameterFilter;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.ValueSource;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;

import java.util.LinkedList;
import java.util.List;

/**
 * Describes wrapper parameter binding rules.
 * Describes a rule for assignment of a value from {@code valueSource} to
 * parameters matching {@code filter}.
 */
public class PartialMapperImpl implements PartialMapper {

    private final ParameterFilter filter;
    private final ValueSource valueSource;

    /**
     *
     * @param filter Selects <b>origin</b> method parameters to pass the value to.
     * @param valueSource Source for the value.
     */
    public PartialMapperImpl(ParameterFilter filter, ValueSource valueSource) {
        this.filter = filter;
        this.valueSource = valueSource;
    }

    /**
     * Evaluates list of {@code PartialDescriptor} objects for the arguments
     * matching {@code filter}. Each PartialDescriptor specifies a value
     * source for an argument.
     *
     * @param originMethod Method to get argument binders
     * @return List of Partial Descriptors for each argument matching {@code filter}.
     */
    @Override
    public List<PartialDescriptor> getArgumentBinders(MethodDescription originMethod) {

        List<PartialDescriptor> result = new LinkedList<>();

        int parametersCount = originMethod.getParameters().size();

        for (int originIndex = 0; originIndex < parametersCount; originIndex++) {

            if (filter.useArgument(originMethod, originIndex)) {
                TypeDescription originArgType = originMethod
                        .getParameters()
                        .get(originIndex)
                        .getType()
                        .asErasure();

                valueSource.getSource(originIndex, originArgType).ifPresent(result::add);
            }
        }

        return result;
    }
}
