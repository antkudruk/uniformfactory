package com.github.antkudruk.uniformfactory.singleton.argument.filters;

import net.bytebuddy.description.method.MethodDescription;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class ParameterFilterTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface Mark {
        String value() default "";
    }

    static class OriginClass {
        @SuppressWarnings({"WeakerAccess", "unused"})
        public void first(@Mark("hz") String alpha, @Mark("t") Long beta,
                          Boolean gamma, String delta) {

        }
    }

    @Test
    public void anyArgumentTest() throws Exception {
        ParameterQueryBuilder builder = new ParameterQueryBuilder();

        MethodDescription method = new MethodDescription.ForLoadedMethod(
                OriginClass.class.getDeclaredMethod("first",
                        String.class, Long.class, Boolean.class, String.class));

        assertTrue(builder.useArgument(method, 0));
        assertTrue(builder.useArgument(method, 1));
        assertTrue(builder.useArgument(method, 2));
        assertTrue(builder.useArgument(method, 3));
    }

    @Test
    public void hasTypeStringTest() throws Exception {
        ParameterQueryBuilder builder = new ParameterQueryBuilder();
        builder = builder.hasType(String.class);

        MethodDescription method = new MethodDescription.ForLoadedMethod(
                OriginClass.class.getDeclaredMethod("first",
                        String.class, Long.class, Boolean.class, String.class));

        assertTrue(builder.useArgument(method, 0));
        assertFalse(builder.useArgument(method, 1));
        assertFalse(builder.useArgument(method, 2));
        assertTrue(builder.useArgument(method, 3));
    }

    @Test
    public void hasTypeLongTest() throws Exception {
        ParameterQueryBuilder builder = new ParameterQueryBuilder();
        builder.hasType(Long.class);

        MethodDescription method = new MethodDescription.ForLoadedMethod(
                OriginClass.class.getDeclaredMethod("first",
                        String.class, Long.class, Boolean.class, String.class));

        assertFalse(builder.useArgument(method, 0));
        assertTrue(builder.useArgument(method, 1));
        assertFalse(builder.useArgument(method, 2));
        assertFalse(builder.useArgument(method, 3));
    }

    @Test
    public void hasAnnotation() throws Exception {
        ParameterQueryBuilder builder = new ParameterQueryBuilder();
        builder.annotatedWith(Mark.class);

        MethodDescription method = new MethodDescription.ForLoadedMethod(
                OriginClass.class.getDeclaredMethod("first",
                        String.class, Long.class, Boolean.class, String.class));

        assertTrue(builder.useArgument(method, 0));
        assertTrue(builder.useArgument(method, 1));
        assertFalse(builder.useArgument(method, 2));
        assertFalse(builder.useArgument(method, 3));
    }

    @Test
    public void hasAnnotationWithParameter() throws Exception {
        ParameterQueryBuilder builder = new ParameterQueryBuilder();
        builder = builder.annotatedWith(Mark.class, a -> "hz".equals(a.value()));

        MethodDescription method = new MethodDescription.ForLoadedMethod(
                OriginClass.class.getDeclaredMethod("first",
                        String.class, Long.class, Boolean.class, String.class));

        assertTrue(builder.useArgument(method, 0));
        assertFalse(builder.useArgument(method, 1));
        assertFalse(builder.useArgument(method, 2));
        assertFalse(builder.useArgument(method, 3));
    }

    @Test
    public void hasAnnotationAndArgumentType() throws Exception {
        ParameterQueryBuilder builder = new ParameterQueryBuilder();

        builder.annotatedWith(Mark.class)
                .hasType(String.class);

        MethodDescription method = new MethodDescription.ForLoadedMethod(
                OriginClass.class.getDeclaredMethod("first",
                        String.class, Long.class, Boolean.class, String.class));

        assertTrue(builder.useArgument(method, 0));
        assertFalse(builder.useArgument(method, 1));
        assertFalse(builder.useArgument(method, 2));
        assertFalse(builder.useArgument(method, 3));
    }

    @Test
    public void parameterQueryBuilderAddUseParameter() {
        parameterQueryBuilderAdd(true);
    }

    @Test
    public void parameterQueryBuilderAddDoNotUseParameter() {
        parameterQueryBuilderAdd(false);
    }

    private void parameterQueryBuilderAdd(boolean useParameter) {
        ParameterQueryBuilder builder = new ParameterQueryBuilder();

        ParameterFilter parameterFilter = PowerMockito.mock(ParameterFilter.class);
        PowerMockito.when(parameterFilter.useArgument(any(), eq(0)))
                .thenReturn(useParameter);

        builder = builder.add(parameterFilter);

        assertEquals(useParameter, builder.useArgument(null, 0));
    }
}
