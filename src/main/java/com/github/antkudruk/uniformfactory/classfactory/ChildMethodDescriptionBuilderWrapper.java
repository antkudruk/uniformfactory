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

package com.github.antkudruk.uniformfactory.classfactory;


import com.github.antkudruk.uniformfactory.base.Builds;
import com.github.antkudruk.uniformfactory.base.MethodDescriptor;
import lombok.AllArgsConstructor;

/**
 * Enables being able to return ro the parent builder
 */
@AllArgsConstructor
public class ChildMethodDescriptionBuilderWrapper<W> {
    private final ClassFactory.Builder<W> parentBuilder;
    private final Builds<MethodDescriptor> builder;

    /**
     * Ends description of the method.
     * @return Class Factory builder
     */
    public ClassFactory.Builder<W> endMethodDescription() {
        parentBuilder.addMethodDescriptor(builder.build());
        return parentBuilder;
    }
}
