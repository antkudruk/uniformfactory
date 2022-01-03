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

package com.github.antkudruk.uniformfactory.methodcollection.seletor;

import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;

import java.util.Collections;
import java.util.List;

/**
 * Provides collections of methods and fields to generate a collection of
 * functional objects to fork with class members the common way.
 */
public interface MemberSelector {
    /**
     * Provides a list of methods to work with. Basically, it's methods
     * annotated with a specified annotation.
     *
     * @param type
     * @return
     */
    default List<MethodDescription> getMethods(TypeDescription type) {
        return Collections.emptyList();
    }

    /**
     * Provides a list of fields to work with. Basically, it's fields
     * annotated with a specified annotation.
     *
     * @param type
     * @return
     */
    default List<FieldDescription> getFields(TypeDescription type) {
        return Collections.emptyList();
    }
}