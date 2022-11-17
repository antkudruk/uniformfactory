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

package com.github.antkudruk.uniformfactory.singleton.argument.valuesource;

import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.AnyParameterFilter;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialDescriptor;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialMapperImpl;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.partieldescriptor.PartialParameterDescriptor;
import com.github.antkudruk.uniformfactory.singleton.argument.filters.ParameterFilter;
import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.AnnotationParameterFilter;
import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.ParameterTypeFilter;
import com.github.antkudruk.uniformfactory.singleton.argument.typemapper.ExtendsParameterTranslator;
import com.github.antkudruk.uniformfactory.singleton.argument.typemapper.ParameterMappersCollection;
import com.github.antkudruk.uniformfactory.singleton.argument.typemapper.SuperParameterTranslator;
import net.bytebuddy.description.type.TypeDescription;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.function.Function;

/**
 * Provides parameter from the <b>wrapper</b> method to the <b>origin</b>
 * method.
 *
 * @param <N> Wrapper parameter class.
 */
public class ParameterValue<N> implements ValueSource {

    private final int wrapperParameterIndex;
    private final ParameterMappersCollection<N> mapper;

    public ParameterValue(Class<N> wrapperParameterClass, int wrapperParameterIndex) {
        this(wrapperParameterIndex,
                new ParameterMappersCollection<>(wrapperParameterClass));
    }

    private ParameterValue(int wrapperParameterIndex,
                           ParameterMappersCollection<N> mapper) {
        this.wrapperParameterIndex = wrapperParameterIndex;
        this.mapper = mapper;
    }

    public ParameterValue<N> addTranslatorForExtends(TypeDescription originClass,
                                                     Function<N, ?> parameterMapper) {
        mapper.add(new ExtendsParameterTranslator<>(originClass, parameterMapper));
        return this;
    }

    public ParameterValue<N> addTranslatorForSuper(TypeDescription originClass,
                                                   Function<N, ?> parameterMapper) {
        mapper.add(new SuperParameterTranslator<>(originClass, parameterMapper));
        return this;
    }

    @Override
    public Optional<PartialDescriptor> getSource(
            int originIndex,
            TypeDescription originParameterType) {

        return mapper
                .findSuitableTranslator(originParameterType)
                .map(p -> new PartialParameterDescriptor<>(
                                originIndex,
                                wrapperParameterIndex,
                                p
                        )
                );
    }

    /**
     * @param <W> Wrapper parameter class (parameter source class)
     * @param <T> Parent wrapper type
     */
    public static class ShortcutBuilder<W, T extends HasParameterTranslator> {

        private final T parentBuilder;
        private final Class<W> wrapperParameterClass;
        private final int parameterNumber;

        public ShortcutBuilder(T parentBuilder, Class<W> wrapperParameterClass, int parameterNumber) {
            this.parentBuilder = parentBuilder;
            this.wrapperParameterClass = wrapperParameterClass;
            this.parameterNumber = parameterNumber;
        }

        public WithDefinedTargets applyTo(ParameterFilter filter) {
            return new WithDefinedTargets(filter);
        }

        public WithDefinedTargets applyToAny() {
            return new WithDefinedTargets(new AnyParameterFilter());
        }

        public WithDefinedTargets applyToAnnotated(Class<? extends Annotation> parameterAnnotation) {
            return applyTo(new AnnotationParameterFilter<>(parameterAnnotation));
        }

        // TODO test and add example into the doc.
        public WithDefinedTargets applyToTyped(Class<?> parameterClass) {
            return applyTo(new ParameterTypeFilter<>(parameterClass));
        }

        public class WithDefinedTargets {

            private final ParameterFilter filter;
            private ParameterMappersCollection<W> parameterMapper
                    = new ParameterMappersCollection<>(wrapperParameterClass);

            WithDefinedTargets(ParameterFilter filter) {
                this.filter = filter;
            }

            public WithDefinedTargets setMapper(ParameterMappersCollection<W> parameterMapper) {
                this.parameterMapper = parameterMapper.createChild();
                return this;
            }

            public <K> WithDefinedTargets addTranslator(Class<K> originParameterClass, Function<W, K> translator) {
                parameterMapper.add(new ExtendsParameterTranslator<>(originParameterClass, translator));
                return this;
            }

            public <K> WithDefinedTargets addSuper(Class<K> originParameterClass, Function<W, K> translator) {
                parameterMapper.add(new SuperParameterTranslator<>(originParameterClass, translator));
                return this;
            }

            public T finishParameterDescription() {
                parentBuilder.addParameterTranslator(new PartialMapperImpl(filter,
                        new ParameterValue<>(parameterNumber, parameterMapper)));
                return parentBuilder;
            }
        }
    }
}
