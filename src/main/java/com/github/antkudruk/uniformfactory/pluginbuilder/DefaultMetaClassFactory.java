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

import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import net.bytebuddy.description.type.TypeDescription;

import java.lang.reflect.Constructor;
import java.util.function.Function;

/**
 * @param <W> Wrapper class
 */
public class DefaultMetaClassFactory<W> implements MetaClassFactory<W> {

    private final ClassFactory<W> classFactory;

    public DefaultMetaClassFactory(ClassFactory<W> classFactory) {
        this.classFactory = classFactory;
    }

    @Override
    public <O> Function<O, ? extends W> generateMetaClass(Class<O> originClass) {
        try {
            Constructor<? extends W> wrapperConstructor = classFactory
                    .build(new TypeDescription.ForLoadedType(originClass))
                    .load(DefaultMetaClassFactory.class.getClassLoader())
                    .getLoaded()
                    .getConstructor(originClass);

            return new WrapperObjectGenerator<>(wrapperConstructor);
        } catch (ClassGeneratorException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static class WrapperObjectGenerator<O, W> implements Function<O, W> {

        private final Constructor<? extends W> wrapperConstructor;

        public WrapperObjectGenerator(Constructor<? extends W> wrapperConstructor) {
            this.wrapperConstructor = wrapperConstructor;
        }

        @Override
        public W apply(O t) {
            try {
                return wrapperConstructor.newInstance(t);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException();
            }
        }
    }
}
