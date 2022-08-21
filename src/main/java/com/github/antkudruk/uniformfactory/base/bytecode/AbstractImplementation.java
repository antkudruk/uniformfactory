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

package com.github.antkudruk.uniformfactory.base.bytecode;

import net.bytebuddy.dynamic.scaffold.InstrumentedType;

public abstract class AbstractImplementation extends AbstractTerminatableImplementation {

    AbstractImplementation(boolean terminate) {
        super(terminate);
    }

    @Override
    public final InstrumentedType prepare(InstrumentedType instrumentedType) {
        return instrumentedType;
    }
}
