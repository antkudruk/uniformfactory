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

import net.bytebuddy.implementation.Implementation;

/**
 * Bytecode implementation that may terminate method with {@code return}
 * statement if it's the last
 */
public abstract class AbstractTerminatableImplementation implements Implementation.Composable {

    private final boolean terminate;

    AbstractTerminatableImplementation(boolean terminate) {
        this.terminate = terminate;
    }

    protected abstract AbstractTerminatableImplementation cloneNotTerminated();

    @Override
    public final Implementation andThen(Implementation implementation) {
        AbstractTerminatableImplementation notTerminated = cloneNotTerminated();

        if (notTerminated.terminate) {
            throw new RuntimeException("Intermediate bytecode block can not contain return state");
        }

        return new Compound(notTerminated, implementation);
    }

    @Override
    public final Composable andThen(Composable implementation) {
        AbstractTerminatableImplementation notTerminated = cloneNotTerminated();

        if (notTerminated.terminate) {
            throw new RuntimeException("Intermediate bytecode block can not contain return state");
        }

        return new Compound.Composable(cloneNotTerminated(), implementation);
    }

    @SuppressWarnings("WeakerAccess")
    protected final boolean isTerminating() {
        return terminate;
    }
}
