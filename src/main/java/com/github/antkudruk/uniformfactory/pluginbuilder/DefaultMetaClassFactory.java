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

package com.github.antkudruk.uniformfactory.pluginbuilder;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.function.Function;

/**
 * @param <W> Wrapper class
 */
@RequiredArgsConstructor
public class DefaultMetaClassFactory<W> implements MetaClassFactory<W> {

    private final ClassFactory<W> classFactory;

    @Override
    @SneakyThrows(ClassGeneratorException.class)
    public <O> Function<O, ? extends W> generateMetaClass(Class<O> originClass) {
        return classFactory.buildWrapperFactory(originClass);
    }
}
