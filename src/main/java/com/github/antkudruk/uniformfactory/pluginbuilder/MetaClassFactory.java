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

package com.github.antkudruk.uniformfactory.pluginbuilder;

import java.util.function.Function;

/**
 * Wrapper Class Generator.
 *
 * @param <W> Wrapper interface
 */
public interface MetaClassFactory<W> {

    /**
     * Returns <b>Wrapper Object Generator</b> for each {@code originClass}.
     * Wrapper Object Generator is a function that creates a wrapper object for origin object.
     *
     * @param originClass Origin class
     * @param <O> Origin class
     * @return Metaclass - a function to generate wrapper objects.
     */
    <O> Function<O, ? extends W> generateMetaClass(Class<O>  originClass);
}
