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

package com.github.antkudruk.uniformfactory.common;

import net.bytebuddy.description.ByteCodeElement;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.matcher.FilterableList;

import java.util.Optional;

public class TypeDescriptionShortcuts {

    public static Optional<MethodDescription> deepFindMethod(
            TypeDescription type,
            String name,
            Class<?>... parameters
    ) {
        Optional<MethodDescription> md = findMethod(
                type,
                name,
                parameters
        );

        if (!md.isPresent()) {
            if (type.getSuperClass() != null) {
                return deepFindMethod(type.getSuperClass().asErasure(), name, parameters);
            } else {
                return Optional.empty();
            }
        } else {
            return md;
        }
    }

    public static Optional<MethodDescription> findMethod(
            Class<?> type,
            String name,
            Class<?>... parameters
    ) {
        return findMethod(
                new TypeDescription.ForLoadedType(type),
                name,
                parameters);
    }

    private static Optional<MethodDescription> findMethod(
            TypeDescription type,
            String name,
            Class<?>... parameters) {
        return toOptional(type
                .getDeclaredMethods()
                .filter(ElementMatchers.named(name)
                        .and(ElementMatchers.takesArguments(parameters))))
                .map(ByteCodeElement.TypeDependant::asDefined);
    }

    public static MethodDescription findMethodByName(
            TypeDescription type,
            String name) {
        return type
                .getDeclaredMethods()
                .filter(ElementMatchers.named(name))
                .getOnly();
    }

    public static Optional<MethodDescription.InDefinedShape> findStaticMethod(
            Class type,
            Class<?> returnType,
            String name,
            Class<?>... parameterTypes) {
        return findStaticMethod(new TypeDescription.ForLoadedType(type),
                returnType,
                name,
                parameterTypes
        );
    }

    private static Optional<MethodDescription.InDefinedShape> findStaticMethod(
            TypeDescription type,
            Class<?> returnType,
            String name,
            Class<?>... parameterTypes) {

        ElementMatcher.Junction<MethodDescription> matcher = ElementMatchers.isStatic()
                .and(ElementMatchers.named(name));

        if (returnType != null) {
            matcher = matcher.and(ElementMatchers.returns(returnType));
        }

        if (parameterTypes != null && parameterTypes.length > 0) {
            matcher = matcher.and(ElementMatchers.takesArguments(parameterTypes));
        }

        return toOptional(type
                .getDeclaredMethods()
                .filter(matcher));
    }

    public static Optional<MethodDescription.InDefinedShape> findConstructor(
            Class type, Class... parameters) {
        return findConstructor(new TypeDescription.ForLoadedType(type), parameters);
    }

    public static Optional<MethodDescription.InDefinedShape> findConstructor(
            TypeDescription type, Class... parameterTypes) {
        return toOptional(type.getDeclaredMethods()
                .filter(ElementMatchers.isConstructor().and(
                        ElementMatchers.takesArguments(parameterTypes)
                )));
    }

    public static Optional<MethodDescription.InDefinedShape> findConstructor(
            TypeDescription type, TypeDescription... parameterTypes) {
        return toOptional(type.getDeclaredMethods()
                .filter(ElementMatchers.isConstructor().and(
                        ElementMatchers.takesArguments(parameterTypes)
                )));
    }

    private static <T> Optional<T> toOptional(FilterableList<T, ?> singleOrEmpty) {
        return singleOrEmpty.size() == 0
                ? Optional.empty()
                : Optional.of(singleOrEmpty.getOnly());
    }

    /*
    public static FieldDescription findField(
            TypeDefinition declaringType,
            String fieldName) {

        return declaringType
                .getDeclaredFields()
                .filter(ElementMatchers.named(fieldName))
                .getOnly();
    }*/

    public static Optional<FieldDescription> findField(
            TypeDescription declaringType,
            String fieldName) {

        return toOptional(declaringType
                .getDeclaredFields()
                .filter(ElementMatchers.named(fieldName)))
                .map(ByteCodeElement.TypeDependant::asDefined);
    }

    public static Optional<FieldDescription> deepFindStaticField(
            TypeDescription declaringType,
            String fieldName) {

        Optional<FieldDescription> fd = findStaticField(declaringType, fieldName);

        if (!fd.isPresent()) {
            if (declaringType.getSuperClass() != null) {
                return deepFindStaticField(declaringType.getSuperClass().asErasure(), fieldName);
            } else {
                return Optional.empty();
            }
        } else {
            return fd;
        }
    }

    public static Optional<FieldDescription> deepFindField(
            TypeDescription declaringType,
            String fieldName) {

        Optional<FieldDescription> fd = findField(declaringType, fieldName);

        if (!fd.isPresent()) {
            if (declaringType.getSuperClass() != null) {
                return findField(declaringType.getSuperClass().asErasure(), fieldName);
            } else {
                return Optional.empty();
            }
        } else {
            return fd;
        }
    }

    public static FieldDescription deepFindRequiredField(
            TypeDescription declaringType,
            String fieldName) {
        return deepFindField(declaringType, fieldName).orElseThrow(RuntimeException::new);
    }

    private static Optional<FieldDescription> findStaticField(
            TypeDescription declaringType,
            String fieldName) {

        return toOptional(declaringType
                .getDeclaredFields()
                .filter(ElementMatchers.isStatic()
                        .and(ElementMatchers.named(fieldName))))
                .map(ByteCodeElement.TypeDependant::asDefined);
    }

}