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

package com.github.antkudruk.uniformfactory.singleton.descriptors;

import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialMapperImpl;
import net.bytebuddy.description.type.TypeDescription;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Maps actual return type in <b>origin method</b> to the return type of
 * <b>wrapper method</b>. It stores the list of the {@code Function} objects
 * (<b>translators</b> for each return type. For each <b>origin</b> method it
 * looks for the <u>last</u> added <b>translator</b> supplying a type
 * assignable from the <b>origin</b> one. It means that you can override any
 * translator by adding one with the same consuming type. If it can't find the
 * mapper, {@code WrapperMethodTypesException} exception is thrown.
 * It always has a translator for the case when <b>origin</b> return type is
 * the same as <b>wrapper</b> return type that does nothing but just returns
 * the origin result.
 *
 * Keep in mind that order of adding translators matters. For example the
 * following code works logically correct
 *
 * {@code
 * class A {}
 * class B extends A {}
 * resultMapperCollection.addTranslator(A.class, translatorA);  // translatorB is called when origin method return type is A
 * resultMapperCollection.addTranslator(B.class, translatorB);  // translatorB is called when origin method return type is B
 * }
 *
 * while in the following code translatorB is never used.
 *
 * {@code
 * class A {}
 * class B extends A {}
 * resultMapperCollection.addTranslator(B.class, translatorB);  // translatorB is never called
 * resultMapperCollection.addTranslator(A.class, translatorA);  // translatorA is called when origin method return type is A or derived from A
 * }
 *
 * @param <A> Wrapper result type
 * @see PartialMapperImpl
 *
 */
public class ResultMapperCollection<A> {

    private final ResultMapperCollection<A> parent;
    private final List<Entry<?>> entries = new LinkedList<>();
    private final Class<A> wrapperResultType;

    public ResultMapperCollection(Class<A> wrapperResultType) {
        this(wrapperResultType, null);
    }

    private ResultMapperCollection(Class<A> wrapperResultType, ResultMapperCollection<A> parent) {
        addMapper(wrapperResultType, t -> t);
        this.wrapperResultType = wrapperResultType;
        this.parent = parent;
    }

    public <O> ResultMapperCollection<A> addMapper(
            Class<O> originResultClass, Function<O, A> mapper) {

        entries.add(0, new Entry<>(originResultClass, mapper));

        return this;
    }

    @SuppressWarnings("unchecked")
    private <O> Optional<Function<O, A>> getTranslator(TypeDescription originResultClass) {
        return entries
                .stream()
                .filter(t -> originResultClass.asErasure().isAssignableTo(t.getOriginResultClass()))
                .findFirst()
                .map((Function<Entry, Function>) Entry::getTranslator)
                .map(t -> (Function<O, A>) t)
                .map(Optional::of)
                .orElse(Optional.ofNullable(parent).flatMap(t -> t.getTranslator(originResultClass)));
    }

    public <O> Function<O, A> getTranslatorOrThrow(TypeDescription originResultClass) throws WrapperMethodTypesException {
        return this.<O>getTranslator(originResultClass).orElseThrow(() -> new WrapperMethodTypesException(
                "No method return value translator from origin return type "
                        + originResultClass.getTypeName()
                        + " to " + wrapperResultType.getSimpleName(), null));
    }

    public ResultMapperCollection<A> createChild() {
        return new ResultMapperCollection<>(wrapperResultType, this);
    }

    public Class<A> getWrapperReturnType() {
        return wrapperResultType;
    }

    private class Entry<O> {
        private final Class<O> originResultClass;
        private final Function<O, A> translator;

        Entry(Class<O> originResultClass, Function<O, A> translator) {
            this.originResultClass = originResultClass;
            this.translator = translator;
        }

        Class<O> getOriginResultClass() {
            return originResultClass;
        }

        Function<O, A> getTranslator() {
            return translator;
        }
    }
}

