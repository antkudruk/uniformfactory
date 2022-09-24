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

package com.github.antkudruk.uniformfactory.methodlist.descriptors;

import com.github.antkudruk.uniformfactory.base.Builds;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.methodcollection.ElementFactory;
import com.github.antkudruk.uniformfactory.methodcollection.ElementFactoryBuilderParentReference;
import com.github.antkudruk.uniformfactory.methodcollection.GetterElementFactory;
import com.github.antkudruk.uniformfactory.methodcollection.SetterElementFactory;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.MemberSelector;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.MemberSelectorByAnnotation;
import lombok.AllArgsConstructor;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@AllArgsConstructor
public class DefaultListElementSource<F> implements ListElementSource<F> {

    private final MemberSelector memberSelector;
    private final ElementFactory<F> elementFactory;

    @Override
    public List<DynamicType.Unloaded<F>> elements(TypeDescription originType) throws ClassGeneratorException {
        List<DynamicType.Unloaded<F>> functionalMapperClasses = new ArrayList<>();
        for (MethodDescription originMethod : memberSelector.getMethods(originType)) {
            functionalMapperClasses.add(
                    elementFactory.getMethodElement(originType, originMethod).build(originType)
            );
        }

        for (FieldDescription field : memberSelector.getFields(originType)) {
            functionalMapperClasses.add(
                    elementFactory.getFieldElement(originType, field).build(originType)
            );
        }

        return functionalMapperClasses;
    }

    @SuppressWarnings("unchecked")
    public static abstract class AbstractBuilder<F, R,  T extends AbstractBuilder<F, R, T>>
            implements Builds<ListElementSource<F>>, ElementFactoryBuilderParentReference.ParentBuilder<F> {
        private final Class<F> elementType;
        private MemberSelector memberSelector;
        private ElementFactory<F> elementFactory;

        public AbstractBuilder(Class<F> elementType) {
            this.elementType = elementType;
        }

        @SuppressWarnings("unchecked")
        public <M extends Annotation> T setMarkerAnnotation(Class<M> marker) {
            setMemberSelector(new MemberSelectorByAnnotation(marker));
            return (T)this;
        }

        @SuppressWarnings("unchecked")
        public T setMemberSelector(MemberSelector memberSelector) {
            this.memberSelector = memberSelector;
            return (T)this;
        }

        public T setElementFactory(ElementFactory<F> elementFactory) {
            this.elementFactory = elementFactory;
            return (T)this;
        }

        public <A extends Annotation> T setMarkerAnnotation(Class<A> marker, Function<A, String> keyGetter) {
            setMarkerAnnotation(marker);
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
        public ListElementSource<F> build() {
            return new DefaultListElementSource<>(memberSelector, elementFactory);
        }
    }

    public static class Builder<F, R> extends AbstractBuilder<F, R, Builder<F, R>> {

        public Builder(Class<F> elementType) {
            super(elementType);
        }
    }

    public static class ShortcutBuilder<P extends ShortcutBuilder.ParentBuilder<F>, F, R>
            extends AbstractBuilder<F, R, ShortcutBuilder<P, F, R>>
    {

        interface ParentBuilder<F> {
            ParentBuilder<F> setElementSource(ListElementSource<F> build);
        }

        private final P parent;

        public ShortcutBuilder(
                P parent,
                Class<F> elementType) {
            super(elementType);
            this.parent = parent;
        }

        public P endElementSource() {
            parent.setElementSource(build());
            return parent;
        }
    }
}
