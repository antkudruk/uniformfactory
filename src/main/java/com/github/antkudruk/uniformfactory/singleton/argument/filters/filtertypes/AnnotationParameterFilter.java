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

package com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes;

import com.github.antkudruk.uniformfactory.singleton.argument.filters.ParameterFilter;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.function.Function;

/**
 * Selects <b>origin</b> method arguments annotated with annotation
 * of type {@code annotationClass}.
 *
 * @param <A> Annotation class to determine <b>origin</b> field or method.
 */
public class AnnotationParameterFilter<A extends Annotation>
        implements ParameterFilter {

    private final Class<A> annotationClass;
    private final Function<A, Boolean> use;

    public AnnotationParameterFilter(Class<A> annotationClass) {
        this.annotationClass = annotationClass;
        this.use = a -> true;
    }

    public AnnotationParameterFilter(Class<A> annotationClass, Function<A, Boolean> use) {
        this.annotationClass = annotationClass;
        this.use = use;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean useArgument(MethodDescription method, int originParameterIndex) {
        return Optional.ofNullable(method.getParameters().get(originParameterIndex)
                .getDeclaredAnnotations()
                .ofType(annotationClass))
                .map(this::loadAnnotationAssumingItHasBeenLoaded)
                .map(use)
                .orElse(false);
    }

    private <T extends Annotation> T loadAnnotationAssumingItHasBeenLoaded(
            AnnotationDescription.Loadable<T> loadable) {
        return loadable.load();
    }
}
