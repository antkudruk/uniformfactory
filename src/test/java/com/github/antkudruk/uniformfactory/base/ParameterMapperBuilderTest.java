package com.github.antkudruk.uniformfactory.base;

import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.ParameterBindersSource;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialMapper;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialMapperImpl;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.ConstantValue;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.HasParameterTranslator;
import com.github.antkudruk.uniformfactory.singleton.argument.valuesource.ParameterValue;
import org.junit.Test;

import java.lang.annotation.Annotation;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ParameterMapperBuilderTest {

    private static final ParameterBindersSource PARTIAL_PARAMETER_UNION = mock(ParameterBindersSource.class);
    private static final HasParameterTranslator PARENT_OBJECT = mock(HasParameterTranslator.class);

    @Test
    public void setParameterMapper() {
        reset(PARTIAL_PARAMETER_UNION, PARENT_OBJECT);
        // given
        ParameterMapperBuilder builder = new ParameterMapperBuilder(PARENT_OBJECT);

        // when
        HasParameterTranslator result = builder.setParameterMapper(PARTIAL_PARAMETER_UNION);

        //  then
        assertEquals(PARENT_OBJECT, result);
        assertEquals(PARTIAL_PARAMETER_UNION, builder.getParameterMapper());
    }

    @Test
    public void constantSource() {
        reset(PARTIAL_PARAMETER_UNION, PARENT_OBJECT);
        // given
        Class<? extends Annotation> annotation = Annotation.class;
        Object constant = mock(Object.class);
        ParameterMapperBuilder builder = new ParameterMapperBuilder(PARENT_OBJECT);

        // when
        ConstantValue.ShortcutBuilder shortcutBuilder = builder.constantSource(constant);
        HasParameterTranslator parameterMapperBuilder = shortcutBuilder.applyToAnnotated(annotation);
        // then

        verify(PARENT_OBJECT, times(1)).addParameterTranslator(any(PartialMapperImpl.class));
        assertEquals(PARENT_OBJECT, parameterMapperBuilder);
    }

    @Test
    public void parameterSource() {
        reset(PARTIAL_PARAMETER_UNION, PARENT_OBJECT);
        // given
        Class<? extends Annotation> annotation = Annotation.class;
        Class parameterClass = Integer.class;
        ParameterMapperBuilder builder = new ParameterMapperBuilder(PARENT_OBJECT);

        // when
        ParameterValue.ShortcutBuilder shortcutBuilder = builder.parameterSource(parameterClass, 2);
        HasParameterTranslator parameterMapperBuilder = shortcutBuilder
                .applyToAnnotated(annotation)
                .finishParameterDescription();

        // then
        assertEquals(PARENT_OBJECT, parameterMapperBuilder);
        verify(PARENT_OBJECT, times(1)).addParameterTranslator(any(PartialMapperImpl.class));
    }

    @Test
    public void addParameterTranslator() {
        reset(PARTIAL_PARAMETER_UNION, PARENT_OBJECT);
        // given
        ParameterMapperBuilder builder = new ParameterMapperBuilder(PARENT_OBJECT);
        PartialMapper partialMapper = mock(PartialMapper.class);
        builder.setParameterMapper(PARTIAL_PARAMETER_UNION);
        when(PARTIAL_PARAMETER_UNION.add(eq(partialMapper))).thenReturn(null);

        // when
        HasParameterTranslator result = builder.addParameterTranslator(partialMapper);

        // then
        assertEquals(PARENT_OBJECT, result);
        verify(PARTIAL_PARAMETER_UNION, times(1)).add(partialMapper);
    }
}
