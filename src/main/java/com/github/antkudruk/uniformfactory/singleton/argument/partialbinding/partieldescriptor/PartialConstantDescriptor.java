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

package com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.partieldescriptor;

import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialDescriptor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.implementation.MethodCall;

/**
 * Provides particular argument with the constant value.
 *
 * @param <O> Constant type
 */
@RequiredArgsConstructor
public class PartialConstantDescriptor<O> implements PartialDescriptor {

    @Getter
    private final int originIndex;
    private final O constant;

    @Override
    public MethodCall addWith(MethodCall methodCall) {
        return methodCall.with(constant);
    }
}
