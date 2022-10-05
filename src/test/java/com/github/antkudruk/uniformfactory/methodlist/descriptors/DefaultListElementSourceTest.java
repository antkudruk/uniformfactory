package com.github.antkudruk.uniformfactory.methodlist.descriptors;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.methodcollection.ElementFactory;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.MemberSelector;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultListElementSourceTest<F> {

    // fields
    @Mock
    private MemberSelector memberSelector;

    @Mock
    private ElementFactory<F> elementFactory;

    // values
    @Mock
    private TypeDescription origin;

    @Mock
    private FieldDescription fieldDescription;

    @Mock
    private MethodDescription methodDescription;

    @Mock
    private ClassFactory fieldClassFactory;

    @Mock
    private ClassFactory methodClassFactory;

    @Mock
    private DynamicType.Unloaded fieldUnloaded;

    @Mock
    private DynamicType.Unloaded methodUnloaded;

    @InjectMocks
    private DefaultListElementSource<F> testedObject;

    @Before
    public void initMocks() throws ClassGeneratorException {
        when(memberSelector.getFields(eq(origin))).thenReturn(Collections.singletonList(fieldDescription));
        when(memberSelector.getMethods(eq(origin))).thenReturn(Collections.singletonList(methodDescription));
        when(elementFactory.getMethodElement(eq(origin), eq(methodDescription))).thenReturn(methodClassFactory);
        when(elementFactory.getFieldElement(eq(origin), eq(fieldDescription))).thenReturn(fieldClassFactory);
        when(methodClassFactory.build(eq(origin))).thenReturn(methodUnloaded);
        when(fieldClassFactory.build(eq(origin))).thenReturn(fieldUnloaded);
    }

    @Test
    public void whenFieldAndMethod_thenBothAdaptors() throws ClassGeneratorException {
        // when
        List<DynamicType.Unloaded<? extends F>> result = testedObject.elements(origin);

        // then
        assertThat(result, hasItems(fieldUnloaded, methodUnloaded));
    }

    @Test(expected = ClassGeneratorException.class)
    public void whenElementFactoryFieldGenerationException_thenThrowClassGeneratorException() throws ClassGeneratorException {
        // given
        when(elementFactory.getFieldElement(eq(origin), eq(fieldDescription)))
                .thenThrow(new ClassGeneratorException(null, null));

        // when
        testedObject.elements(origin);
    }

    @Test(expected = ClassGeneratorException.class)
    public void whenElementFactoryMethodGenerationException_thenThrowClassGeneratorException() throws ClassGeneratorException {
        // given
        when(elementFactory.getMethodElement(eq(origin), eq(methodDescription)))
                .thenThrow(new ClassGeneratorException(null, null));

        // when
        testedObject.elements(origin);
    }

    @Test(expected = ClassGeneratorException.class)
    public void whenElementFactoryBuildFieldGenerationException_thenThrowClassGeneratorException() throws ClassGeneratorException {
        // given
        when(fieldClassFactory.build(eq(origin)))
                .thenThrow(new ClassGeneratorException(null, null));

        // when
        testedObject.elements(origin);
    }

    @Test(expected = ClassGeneratorException.class)
    public void whenBuildMethodGenerationException_thenThrowClassGeneratorException() throws ClassGeneratorException {
        // given
        when(methodClassFactory.build(eq(origin)))
                .thenThrow(new ClassGeneratorException(null, null));

        // when
        testedObject.elements(origin);
    }
}
