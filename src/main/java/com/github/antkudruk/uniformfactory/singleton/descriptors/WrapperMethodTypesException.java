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

package com.github.antkudruk.uniformfactory.singleton.descriptors;

import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;

/**
 * Arisen if the domain method returns unsupported type.
 * It's a subclass of ClassGeneratorException 'cause domain method return type
 * is known only in the Class Generation stage.
 */
public class WrapperMethodTypesException extends ClassGeneratorException {
    WrapperMethodTypesException(String message, Exception cause) {
        super(message, cause);
    }
}
