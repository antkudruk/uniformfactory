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

package com.github.antkudruk.uniformfactory.base;

import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.ParameterBindersSource;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialMapper;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialParameterUnion;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.ConstantValue;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.HasParameterTranslator;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.ParameterValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implements a part of a builder that contains desccription of method parameter
 * @param <T> Current class that has ParameterMapperBuilder instance is a field
 */
public class ParameterMapperBuilder<T extends HasParameterTranslator> implements HasParameterTranslator {

    private final T parentObject;
    private final List<PartialMapper> parameterMapper = new ArrayList<>();

    public ParameterMapperBuilder(T parentObject) {
        this.parentObject = parentObject;
    }

    private ParameterBindersSource partialParameterUnion = new PartialParameterUnion(Collections.emptyList());

    @SuppressWarnings("unchecked")
    public T setParameterMapper(ParameterBindersSource partialParameterUnion) {
        this.partialParameterUnion = partialParameterUnion;
        return parentObject;
    }

    public T addParameterTranslator(PartialMapper mapper) {
        parameterMapper.add(mapper);
        partialParameterUnion = partialParameterUnion.add(mapper);
        return parentObject;
    }

    public <P> ParameterValue.ShortcutBuilder<P, T> parameterSource(Class<P> parameterType, int parameterNumber) {
        return new ParameterValue.ShortcutBuilder<>(parentObject, parameterType, parameterNumber);
    }

    public <P> ConstantValue.ShortcutBuilder<P, T> constantSource(P constant) {
        return new ConstantValue.ShortcutBuilder<>(parentObject, constant);
    }

    public ParameterBindersSource getParameterMapper() {
        return partialParameterUnion;
    }
}
