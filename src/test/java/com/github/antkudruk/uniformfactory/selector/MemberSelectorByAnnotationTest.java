package com.github.antkudruk.uniformfactory.selector;

import com.github.antkudruk.uniformfactory.seletor.MemberSelectorByAnnotation;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MemberSelectorByAnnotationTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    public @interface Marker {

    }

    @SuppressWarnings("unused")
    private static class TestClass {
        @Marker
        private String annotatedField;

        private String justField;

        @Marker
        private void annotatedMethod() {

        }

        private void justMethod() {

        }
    }

    @Test
    public void test() {

        TypeDescription td = new TypeDescription.ForLoadedType(TestClass.class);

        MemberSelectorByAnnotation memberSelectorByAnnotation
                = new MemberSelectorByAnnotation(Marker.class);

        List<FieldDescription> fields = memberSelectorByAnnotation.getFields(td);
        List<MethodDescription> methods = memberSelectorByAnnotation.getMethods(td);

        assertEquals(1, fields.size());
        assertEquals(fields.get(0).getName(), "annotatedField");

        assertEquals(1, methods.size());
        assertEquals(methods.get(0).getName(), "annotatedMethod");
    }
}
