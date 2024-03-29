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
    private final LinkedList<SingleParameterTranslator<A>> parameterTranslators
            = new LinkedList<>();

    public ParameterMappersCollection(Class<A> wrapperParameterType) {
        this(wrapperParameterType, getDefault(wrapperParameterType));
    }

    private ParameterMappersCollection(Class<A> wrapperParameterType, ParameterMappersCollection<A> parent) {
        this.parent = parent;
        this.parameterClass = wrapperParameterType;
    }

    /**
     * Adds parameter translator from originClass to the wrapper argument type.
     *
     * @param translator Translates the origin class to wrapper parameter class.
     * @return This object as a builder.
     */
    public ParameterMappersCollection<A> add(SingleParameterTranslator<A> translator) {
        parameterTranslators.add(translator);
        return this;
    }

    /**
     * Returns a suitable translator from wrapper parameter type to origin parameter type
     * @param originParameterClass Origin parameter type description
     * @return Suitable parameter descriptor.
     */
    public Optional<Function<A, ?>> findSuitableTranslator(
            TypeDescription originParameterClass) {

        Iterator<SingleParameterTranslator<A>> it = parameterTranslators.descendingIterator();

        while (it.hasNext()) {
            SingleParameterTranslator<A> descriptor = it.next();
            if (descriptor.isApplicable(originParameterClass)) {
                return Optional.of(descriptor).map(SingleParameterTranslator::getTranslator);
            }
        }

        return Optional.ofNullable(parent).flatMap(t -> t.findSuitableTranslator(originParameterClass));
    }

    public ParameterMappersCollection<A> createChild() {
        return new ParameterMappersCollection<>(parameterClass, this);
    }

    private static <A> ParameterMappersCollection<A> getDefault(Class<A> wrapperParameterType) {
        ParameterMappersCollection<A> collection = new ParameterMappersCollection<>(wrapperParameterType, null);
        collection.add(
                new SuperParameterTranslator<>(Object.class, t -> t));
        collection.add(
                new ExtendsParameterTranslator<>(wrapperParameterType, t -> t));
        collection.add(new ExtendsParameterTranslator<>(String.class, Object::toString));
        return collection;
    }
}
