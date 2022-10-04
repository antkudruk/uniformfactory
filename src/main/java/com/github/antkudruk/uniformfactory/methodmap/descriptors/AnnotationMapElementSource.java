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

package com.github.antkudruk.uniformfactory.methodmap.descriptors;

import com.github.antkudruk.uniformfactory.base.Builds;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.methodcollection.ElementFactory;
import com.github.antkudruk.uniformfactory.methodcollection.ElementFactoryBuilderParentReference;
import com.github.antkudruk.uniformfactory.methodcollection.GetterElementFactory;
import com.github.antkudruk.uniformfactory.methodcollection.SetterElementFactory;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.MemberSelector;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.MemberSelectorByAnnotation;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@AllArgsConstructor
public class AnnotationMapElementSource<F> implements MapElementSource<F> {

    @NonNull
    private final Function<MethodDescription, String> methodKeyGetter;
    @NonNull
    private final Function<FieldDescription, String> fieldKeyGetter;
    @NonNull
    private final MemberSelector memberSelector;
    @NonNull
    private final ElementFactory<F> elementFactory;

    @Override
    public Map<String, DynamicType.Unloaded<? extends F>> memberEntries(TypeDescription originType) throws ClassGeneratorException {
        Map<String, DynamicType.Unloaded<? extends F>> functionalMapperClasses = new HashMap<>();

        for (MethodDescription originMethod : memberSelector.getMethods(originType)) {
            functionalMapperClasses.put(
                    methodKeyGetter.apply(originMethod),
                    elementFactory.getMethodElement(originType, originMethod).build(originType)
            );
        }

        for (FieldDescription field : memberSelector.getFields(originType)) {
            functionalMapperClasses.put(
                    fieldKeyGetter.apply(field),
                    elementFactory.getFieldElement(originType, field).build(originType)
            );
        }
        return functionalMapperClasses;
    }

    @SuppressWarnings("unchecked")
    public static abstract class AbstractBuilder<F, T extends AbstractBuilder<F, T>>
            implements Builds<MapElementSource<F>>, ElementFactoryBuilderParentReference.ParentBuilder<F> {

        private MemberSelector memberSelector;
        private Function<MethodDescription, String> methodKeyGetter;
        private Function<FieldDescription, String> fieldKeyGetter;
        private ElementFactory<F> elementFactory;
        private final Class<F> elementType;

        public AbstractBuilder(Class<F> elementType) {
            this.elementType = elementType;
        }

        public <M extends Annotation> T setMarkerAnnotation(Class<M> marker) {
            setMemberSelector(new MemberSelectorByAnnotation(marker));
            return (T)this;
        }

        public T setMethodKeyGetter(Function<MethodDescription, String> methodKeyGetter) {
            this.methodKeyGetter = methodKeyGetter;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T setMemberSelector(MemberSelector memberSelector) {
            this.memberSelector = memberSelector;
            return (T)this;
        }

        public T setFieldKeyGetter(Function<FieldDescription, String> fieldKeyGetter) {
            this.fieldKeyGetter = fieldKeyGetter;
            return (T) this;
        }

        public T setElementFactory(ElementFactory<F> elementFactory) {
            this.elementFactory = elementFactory;
            return (T) this;
        }

        public <A extends Annotation> T setMarkerAnnotation(Class<A> marker, Function<A, String> keyGetter) {
            setMarkerAnnotation(marker);
            setMethodKeyGetter(md -> keyGetter.apply(Objects.requireNonNull(md.getDeclaredAnnotations().ofType(marker)).load()));
            setFieldKeyGetter(fd -> keyGetter.apply(Objects.requireNonNull(fd.getDeclaredAnnotations().ofType(marker)).load()));
            return (T) this;
        }

        public <R> GetterElementFactory.ShortcutBuilder<T, F, R>
                getterElementFactory(Class<R> resultType) {
            return new GetterElementFactory.ShortcutBuilder<>(
                    (T) this,
                    elementType,
                    resultType);
        }

        public SetterElementFactory.ShortcutBuilder<T, F> setterElementFactory() {
            return new SetterElementFactory.ShortcutBuilder<>(
                    (T) this,
                    elementType);
        }

        @Override
        public MapElementSource<F> build() {
            return new AnnotationMapElementSource<>(
                    methodKeyGetter,
                    fieldKeyGetter,
                    memberSelector,
                    elementFactory);
        }
    }

    public static class Builder<F> extends AbstractBuilder<F, Builder<F>> {
        public Builder(Class<F> elementType) {
            super(elementType);
        }
    }

    public static class ShortcutBuilder<P extends ShortcutBuilder.ParentBuilder<F>, F>
            extends AbstractBuilder<F, ShortcutBuilder<P, F>> {

        interface ParentBuilder<F> {
            ParentBuilder<F> setMapElementSource(MapElementSource<F> elementSource);
        }

        private final P parent;

        public ShortcutBuilder(
                P parent,
                Class<F> elementType) {
            super(elementType);
            this.parent = parent;
        }

        public P endElementSource() {
            parent.setMapElementSource(build());
            return parent;
        }
    }
}
