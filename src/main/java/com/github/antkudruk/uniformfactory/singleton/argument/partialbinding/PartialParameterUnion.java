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

import com.github.antkudruk.uniformfactory.singleton.argument.exceptions.ParameterTranslatorNotFound;
import net.bytebuddy.description.method.MethodDescription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PartialParameterUnion implements ParameterBindersSource {

    private final PartialMapper[] list;

    public PartialParameterUnion(PartialMapper... list) {
        this.list = list;
    }

    /**
     * Returns binders for each parameter with the corresponding index.
     *
     * @param originMethod origin method.
     * @return List of parameter translators for the corresponding origin
     * method parameters.
     * @throws ParameterTranslatorNotFound Arisen if there are parameters
     *                                     without result translators.
     */
    @Override
    public final List<PartialDescriptor> getParameterBinders(
            MethodDescription originMethod) throws ParameterTranslatorNotFound {

        int counter = originMethod.getParameters().size();

        List<PartialDescriptor> binders = new ArrayList<>(Collections.nCopies(counter, null));

        for (PartialMapper b : list) {
            for (PartialDescriptor n : b.getArgumentBinders(originMethod)) {
                /* TODO: Add control that origin parameter type matches wrapper parameter type.
                Currently that's PartialDescriptor tesponsible for it.
                */
                if (binders.get(n.getOriginIndex()) == null) {
                    counter--;
                }

                binders.set(n.getOriginIndex(), n);
            }
        }

        if (counter != 0) {
            String missingArguments = IntStream.range(0, binders.size())
                    .boxed()
                    .filter(i -> binders.get(i) == null)
                    .map(t -> "  Method: " + originMethod.getName() + ", argument: " + t + " : "
                            + originMethod.getParameters().get(t).getType().getTypeName()
                            + " " + originMethod.getParameters().get(t).getInternalName()
                    )
                    .collect(Collectors.joining(",\n"));

            throw new ParameterTranslatorNotFound("Binders for the following arguments are not specified:\n"
                    + missingArguments, null);
        }

        return binders;
    }

    public static class Builder {
        List<PartialMapper> mappers = new LinkedList<>();

        public Builder add(PartialMapper partialMapper) {
            mappers.add(partialMapper);
            return this;
        }

        public PartialParameterUnion build() {
            return new PartialParameterUnion(mappers.toArray(new PartialMapper[0]));
        }
    }
}
