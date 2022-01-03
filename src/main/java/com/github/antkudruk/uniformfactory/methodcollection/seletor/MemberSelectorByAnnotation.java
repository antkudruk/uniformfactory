/*
    Copyright 2020 - 2021 Anton Kudruk

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

package com.github.antkudruk.uniformfactory.methodcollection.seletor;

import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class MemberSelectorByAnnotation implements MemberSelector {

    private final Class<? extends Annotation> memberMarkerAnnotation;

    public MemberSelectorByAnnotation(Class<? extends Annotation> memberMarkerAnnotation) {
        this.memberMarkerAnnotation = memberMarkerAnnotation;
    }

    @Override
    public List<MethodDescription> getMethods(TypeDescription type) {
        return new ArrayList<>(type.getDeclaredMethods().filter(ElementMatchers.isAnnotatedWith(memberMarkerAnnotation)));
    }

    @Override
    public List<FieldDescription> getFields(TypeDescription type) {
        return new ArrayList<>(type.getDeclaredFields().filter(ElementMatchers.isAnnotatedWith(memberMarkerAnnotation)));
    }
}
