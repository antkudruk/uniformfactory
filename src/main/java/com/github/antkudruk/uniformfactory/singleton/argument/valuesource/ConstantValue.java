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

import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.AnnotationParameterFilter;
import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.ParameterTypeFilter;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialMapperImpl;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.partieldescriptor.PartialConstantDescriptor;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialDescriptor;
import net.bytebuddy.description.type.TypeDescription;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * Provides constant parameter to the <b>origin</b>.
 * @param <O> Constant (and wrapper method) parameter.
 */
public class ConstantValue<O> implements ValueSource {

    private final O constant;

    public ConstantValue(O constant) {
        this.constant = constant;
    }

    @Override
    public Optional<PartialDescriptor> getSource(int originIndex, TypeDescription originArgumentClass) {
        return originArgumentClass.isAssignableFrom(constant.getClass())
                ? Optional.of(new PartialConstantDescriptor<>(originIndex, constant))
                : Optional.empty();
    }

    /**
     * @param <W> Wrapper parameter class (parameter source class)
     * @param <T> Parent wrapper type
     */
    public static class ShortcutBuilder<W, T extends HasParameterTranslator> {
        private final T parentBuilder;
        private final W wrapperParameterValue;

        public ShortcutBuilder(T parentBuilder, W wrapperParameterValue) {
            this.parentBuilder = parentBuilder;
            this.wrapperParameterValue = wrapperParameterValue;
        }

        /**
         * In the origin method call applies the constant to parameters with the class {@code parameterClass}
         * @param parameterClass Class on the parameter.
         * @return parent builder
         */
        public T applyToTyped(Class<?> parameterClass) {
            parentBuilder.addParameterTranslator(new PartialMapperImpl(
                    new ParameterTypeFilter<>(parameterClass),
                    new ConstantValue<>(wrapperParameterValue)
            ));
            return parentBuilder;
        }

        /**
         * In the origin method call applies the constant to parameters annotated with {@code annotation}
         * @param annotation Annotation that marks the parameter to pass it to.
         * @return parent builder
         */
        public T applyToAnnotated(Class<? extends Annotation> annotation) {
            parentBuilder.addParameterTranslator(new PartialMapperImpl(
                    new AnnotationParameterFilter<>(annotation),
                    new ConstantValue<>(wrapperParameterValue)
            ));
            return parentBuilder;
        }
    }
}
