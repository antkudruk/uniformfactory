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

package com.github.antkudruk.uniformfactory.singleton.argument.exceptions;

import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;

/**
 * Thrown when we face an argument in the origin method that has a type that doesn't
 * have an appropriate mapper.
 */
public class ParameterTranslatorNotFound extends ClassGeneratorException {

    public ParameterTranslatorNotFound(Exception cause) {
        super(cause);
    }

    public ParameterTranslatorNotFound(String message, Exception cause) {
        super(message, cause);
    }
}
