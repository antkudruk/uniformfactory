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

package com.github.antkudruk.uniformfactory.singleton.argument.filters;

import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.AnnotationParameterFilter;
import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.ParameterTypeFilter;
import net.bytebuddy.description.method.MethodDescription;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Defines criteria for the <b>Origin</b> method parameters to be passed in.
 */
public class ParameterQueryBuilder implements ParameterFilter {

    private final List<ParameterFilter> filters = new LinkedList<>();

    public ParameterQueryBuilder add(ParameterFilter filter) {
        filters.add(filter);
        return this;
    }

    /**
     * Selects only these parameters that can be cast to the specified class.
     * <p>
     * Shortcut for add(new ParameterTypeFilter&lt;&gt;( . . . ))
     *
     * @param parameterClass Expected type
     * @return This object as a builder.
     */
    public ParameterQueryBuilder hasType(Class<?> parameterClass) {
        add(new ParameterTypeFilter<>(parameterClass));
        return this;
    }

    /**
     * Selects only these parameters annotated by the particular annotation.
     * <p>
     * Shortcut for add(new AnnotationParameterFilter&lt;&gt;( . . .  ))
     *
     * @param annotationClass Expected annotation.
     * @param <A>             Class of the annotation.
     * @return This object as a builder
     */
    public <A extends Annotation> ParameterQueryBuilder annotatedWith(
            Class<A> annotationClass) {
        add(new AnnotationParameterFilter<>(annotationClass));
        return this;
    }

    public <A extends Annotation> ParameterQueryBuilder annotatedWith(
            Class<A> annotationClass, Function<A, Boolean> isOk) {

        add(new AnnotationParameterFilter<>(annotationClass, isOk));
        return this;
    }

    @Override
    public boolean useArgument(MethodDescription method, int originParameterIndex) {
        return filters.stream().allMatch(f -> f.useArgument(method, originParameterIndex));
    }
}
