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

import com.github.antkudruk.uniformfactory.base.Builds;
import lombok.AllArgsConstructor;

/**
 *
 * @param <M> Parent method descriptor
 */
@AllArgsConstructor
public class ElementFactoryBuilderParentReference<F, M extends ElementFactoryBuilderParentReference.ParentBuilder<F>> {

    public interface ParentBuilder<F> {
        ParentBuilder<F> setElementFactory(ElementFactory<F> elementFactory);
    }

    private final M parentElementFactoryBuilder;
    private final Builds<ElementFactory<F>> elementFactoryBuilder;

    public M finishElementFactory() {
        parentElementFactoryBuilder.setElementFactory(elementFactoryBuilder.build());
        return parentElementFactoryBuilder;
    }
}
