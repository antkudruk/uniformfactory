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

package com.github.antkudruk.uniformfactory.singleton.argument.typemapper;

import net.bytebuddy.description.type.TypeDescription;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Function;

/**
 * Mapper class chooses an appropriate <b>translator</b> for the corresponding
 * type of the origin method parameter. <b>Translator</b> is a {@code Function}
 * translating the variable from the <b>wrapper</b> parameter type to the
 * <b>origin</b> parameter type.
 * The algorithm that chooses an appropriate translator works in the following way.
 * <ul>
 *     <li>
 *         It chooses an appropriate translator if the <b>wrapper</b> parameter
 *         can be applied to the <b>origin</b> parameter (i. e. if it is inherited
 *         from the class the <b>origin</b> parameter has)
 *     </li>
 *     <li>
 *         The last added translator thar can be applied to the <b>origin</b> parameter
 *         has the higher priority that the previous translators.
 *     </li>
 *     <li>
 *         {@code ParameterMappersCollection} has <b>repeating</b> translator
 *         (@code t-&gt;t}) and <b>toString</b> translator ({@code Object::toString})
 *         by default.
 *     </li>
 * </ul>
 *
 * @param <A> Wrapper argument type
 */
public class ParameterMappersCollection<A> {

    private final ParameterMappersCollection<A> parent;
    private final Class<A> parameterClass;
    private final LinkedList<ParameterMapperDescriptor<A>> parameterTranslators
            = new LinkedList<>();

    public ParameterMappersCollection(Class<A> wrapperParameterType) {
        this(wrapperParameterType, null);
    }

    private ParameterMappersCollection(Class<A> wrapperParameterType, ParameterMappersCollection<A> parent) {
        this.parent = parent;
        this.parameterClass = wrapperParameterType;
        parameterTranslators.add(
                new ParameterMapperDescriptor<>(wrapperParameterType, t -> t));
        parameterTranslators.add(
                new ParameterMapperDescriptor<>(String.class, Object::toString));
    }

    /**
     * Adds parameter translator from originClass to the wrapper argument type.
     *
     * @param originClass A case of origin parameter type
     * @param translator Translates the origin class to wrapper parameter class.
     * @return This object as a builder.
     */
    public ParameterMappersCollection<A> add(
            TypeDescription originClass, Function<A, ?> translator) {

        parameterTranslators.add(new ParameterMapperDescriptor<>(
                originClass, translator));

        return this;
    }

    public Optional<ParameterMapperDescriptor<A>> findSuitableTranslator(
            TypeDescription originParameterClass) {

        Iterator<ParameterMapperDescriptor<A>> it = parameterTranslators.descendingIterator();

        while (it.hasNext()) {
            ParameterMapperDescriptor<A> descriptor = it.next();
            if (descriptor.getOriginParameterClass().isAssignableFrom(originParameterClass)) {
                return Optional.of(descriptor);
            }
        }

        return Optional.ofNullable(parent)
                .map(t -> t.findSuitableTranslator(originParameterClass))
                .map(t -> t.orElse(null));
    }

    public ParameterMappersCollection<A> createChild() {
        return new ParameterMappersCollection<>(parameterClass, this);
    }

    /**
     * {@code ParameterMappersCollection} entry.
     *
     * @param <A> Wrapper parameter class.
     */
    public static class ParameterMapperDescriptor<A> {
        private final TypeDescription originParameterClass;
        private final Function<A, ?> translator;

        ParameterMapperDescriptor(TypeDescription originParameterClass,
                                  Function<A, ?> translator) {
            this.originParameterClass = originParameterClass;
            this.translator = translator;
        }

        ParameterMapperDescriptor(Class originParameterClass,
                                  Function<A, ?> translator) {
            this.originParameterClass
                    = new TypeDescription.ForLoadedType(originParameterClass);
            this.translator = translator;
        }

        TypeDescription getOriginParameterClass() {
            return originParameterClass;
        }

        public Function<A, ?> getTranslator() {
            return translator;
        }
    }
}
