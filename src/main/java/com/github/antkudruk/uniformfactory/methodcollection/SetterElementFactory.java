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

package com.github.antkudruk.uniformfactory.methodcollection;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.SpecifiedFieldSelector;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialMapper;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;

/**
 * Element factory for setter field
 * @param <F>
 */
public class SetterElementFactory<F> implements ElementFactory<F> {

    private final Class<F> elementType;
    private final PartialMapper parameterMapper;

    public SetterElementFactory(
            Class<F> elementType,
            PartialMapper parameterMapper) {
        this.elementType = elementType;
        this.parameterMapper = parameterMapper;
    }

    @Override
    public ClassFactory<F> getFieldElement(
            TypeDescription origin,
            FieldDescription fieldDescription) {
        return new ClassFactory.ShortcutBuilder<>(elementType)
                .addSetter(elementType.getDeclaredMethods()[0])
                .setMemberSelector(new SpecifiedFieldSelector(fieldDescription))
                .endMethodDescription()
                .build();
    }

    @Override
    public ClassFactory<F> getMethodElement(
            TypeDescription origin,
            MethodDescription methodDescription) {
            throw new RuntimeException("Not Implemented");
    }
}
