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

package com.github.antkudruk.uniformfactory.singleton.atomicaccessor;

import net.bytebuddy.implementation.LoadedTypeInitializer;
import net.bytebuddy.utility.privilege.SetAccessibleAction;

import java.lang.reflect.Field;
import java.security.AccessController;

/**
 * Tool to initiate {@code private static final} fields.
 */
public class ForStaticField implements LoadedTypeInitializer {

    private final String fieldName;
    private final Object value;

    public ForStaticField(String fieldName, Object value) {
        this.fieldName = fieldName;
        this.value = value;
    }

    @Override
    public void onLoad(Class<?> type) {
        try {
            Field field = type.getDeclaredField(fieldName);
            AccessController.doPrivileged(new SetAccessibleAction<>(field));
            field.set(fieldName, value);
        } catch (IllegalAccessException exception) {
            throw new IllegalArgumentException("Cannot access " + fieldName + " from " + type, exception);
        } catch (NoSuchFieldException exception) {
            throw new IllegalStateException("There is no field " + fieldName + " defined on " + type, exception);
        }
    }

    @Override
    public boolean isAlive() {
        return true;
    }
}