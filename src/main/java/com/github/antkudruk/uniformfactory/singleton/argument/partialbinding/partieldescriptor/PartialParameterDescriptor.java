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
import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.CrossLoadersFunctionAdapter;
import com.github.antkudruk.uniformfactory.singleton.atomicaccessor.ForStaticField;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.jar.asm.Opcodes;

import java.util.function.Function;

/**
 * Adds auxiliary object providing parameter from the wrapper method
 * to the origin method with {@code parameterTranslator} applied.
 * @param <O> Origin parameter type.
 * @param <N> Wrapper parameter type.
 */
public class PartialParameterDescriptor<O, N> implements PartialDescriptor {

    private static final String PARAMETER_TRANSLATOR = "PARAMETER_TRANSLATOR_";

    private final String mapperName;
    private final int originIndex;
    private final int wrapperIndex;
    private final Function<N, O> parameterTranslator;

    public PartialParameterDescriptor(int originIndex, int wrapperIndex,
                                      Function<N, O> parameterTranslator) {

        mapperName = PARAMETER_TRANSLATOR + originIndex;
        this.originIndex = originIndex;
        this.wrapperIndex = wrapperIndex;
        this.parameterTranslator = parameterTranslator;
    }

    @Override
    public final int getOriginIndex() {
        return originIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <F> DynamicType.Builder<F> initiate(DynamicType.Builder<F> bbBuilder) {
        return bbBuilder.defineField(mapperName, CrossLoadersFunctionAdapter.class,
                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC)
                .initializer(new ForStaticField(mapperName,
                        new CrossLoadersFunctionAdapter(parameterTranslator)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MethodCall addWith(MethodCall methodCall) {
        try {
            return methodCall.withMethodCall(
                    (MethodCall) MethodCall
                            .invoke(CrossLoadersFunctionAdapter.class
                                    .getDeclaredMethod("apply", Object.class))
                            .onField(mapperName)
                            .withArgument(wrapperIndex)
                            .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC));
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException("Method apply(Object) DOES exist", ex);
        }
    }
}
