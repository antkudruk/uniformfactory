package com.github.antkudruk.uniformfactory.singleton.descriptors;

import com.github.antkudruk.uniformfactory.base.Enhancer;
import com.github.antkudruk.uniformfactory.base.MethodDescriptor;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.MemberSelector;
import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.ParameterBindersSource;
import com.github.antkudruk.uniformfactory.singleton.enhancers.SingletonMethodToFieldEnhancer;
import com.github.antkudruk.uniformfactory.singleton.enhancers.SingletonMethodToMethodEnhancer;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.matcher.FilterableList;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        SingletonMethodToMethodEnhancer.class,
        SingletonMethodToFieldEnhancer.class,
        MethodSingletonDescriptor.class
})
@Ignore
public class MethodSingletonDescriptorTest {

    @Mock
    private ResultMapperCollection<String> resultMapper;

    @Mock
    private ParameterBindersSource partialMapper;

    @Mock
    private TypeDescription originClass;

    @Mock
    private MethodList methodList;

    @Mock
    private FieldList fieldList;

    @Mock
    private MemberSelector memberSelector;

    private static Method wrapperMethod;

    static {
        try {
            wrapperMethod = MethodSingletonDescriptorTest.Wrapper
                    .class
                    .getDeclaredMethod(
                            "concat", String.class, Integer.class);
        } catch (NoSuchMethodException ignore) {
            // ignore
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Marker {

    }

    private static class TestBuilder implements MethodSingletonDescriptor.BuilderInterface<String> {

        private final ParameterBindersSource partialMapper;
        private final ResultMapperCollection<String> resultMapper;
        private final MemberSelector memberSelector;

        TestBuilder(ParameterBindersSource partialMapper, ResultMapperCollection<String> resultMapper, MemberSelector memberSelector) {
            this.partialMapper = partialMapper;
            this.resultMapper = resultMapper;
            this.memberSelector = memberSelector;
        }

        @Override
        public MemberSelector getMemberSelector() {
            return memberSelector;
        }

        @Override
        public Method getWrapperMethod() {
            return wrapperMethod;
        }

        @Override
        public MethodDescriptor build() {
            return new MethodSingletonDescriptor<>(this);
        }

        @Override
        public ResultMapperCollection<String> resultMapper() {
            return resultMapper;
        }

        @Override
        public ParameterBindersSource getParameterMapper() {
            return partialMapper;
        }

        @Override
        public String defaultValue() {
            return null;
        }

        @Override
        public boolean hasDefaultValue() {
            return false;
        }
    }

    public static class OriginImpl {
        @SuppressWarnings("unused")
        @Marker
        public String concat(String name, Integer index) {
            return name + index;
        }
    }

    public interface Wrapper {
        String concat(String name, Integer index);
    }

    @SuppressWarnings("unchecked")
    @Before
    public void setupMocks() {
        PowerMockito.when(originClass.getDeclaredMethods())
                .thenReturn(PowerMockito.mock(MethodList.class,
                        Answers.RETURNS_DEEP_STUBS));
        PowerMockito.when(originClass.getDeclaredMethods())
                .thenReturn(methodList);
        PowerMockito.when(originClass.getDeclaredFields())
                .thenReturn(fieldList);
        PowerMockito.when(resultMapper.getWrapperReturnType()).thenReturn(String.class);
    }

    @SuppressWarnings("unchecked")
    private void mockMethodList(int numberOfMatchedMethods,
                                MethodDescription methodDescription) {
        mockList(MethodList.class, methodList, numberOfMatchedMethods,
                methodDescription);
    }

    @SuppressWarnings("unchecked")
    private void mockFieldList(int numberOfMatchedFields,
                               FieldDescription fieldDescription) {
        mockList(FieldList.class, fieldList, numberOfMatchedFields,
                fieldDescription);
    }

    @SuppressWarnings("unchecked")
    private <E, L extends FilterableList<E, L>> void mockList(
            Class<L> listClass,
            L sourceList,
            int numberOfMatchedFields,
            E fieldDescription) {

        L result = PowerMockito.mock(listClass,
                Answers.RETURNS_DEEP_STUBS);

        PowerMockito.when(result.size())
                .thenReturn(numberOfMatchedFields);
        PowerMockito.when(result.isEmpty())
                .thenReturn(numberOfMatchedFields == 0);
        PowerMockito.when(result.get(eq(0)))
                .thenReturn(fieldDescription);

        PowerMockito.when(sourceList.filter(eq(
                (ElementMatcher<? super E>)
                        ElementMatchers.isAnnotatedWith(Marker.class))))
                .thenReturn(result);
    }

    @Test
    public void testMethodToMethodSingleton() throws Exception {

        SingletonMethodToMethodEnhancer expectedEnhancer =
                mockSingletonMethodToMethodEnhancer();

        MethodDescription.InDefinedShape singletonOriginMethod
                = PowerMockito.mock(MethodDescription.InDefinedShape.class,
                Answers.RETURNS_DEEP_STUBS);

        mockMethodList(1, singletonOriginMethod);
        mockFieldList(0, null);

        MethodDescriptor methodSingletonDescriptor
                = new TestBuilder(partialMapper, resultMapper, memberSelector).build();

        Enhancer enhancer = methodSingletonDescriptor.getEnhancer(originClass);

        assertEquals(expectedEnhancer, enhancer);
        PowerMockito
                .verifyNew(SingletonMethodToMethodEnhancer.class)
                .withArguments(
                        anyString(),
                        eq(originClass),
                        eq(singletonOriginMethod),
                        eq(wrapperMethod),
                        eq(partialMapper),
                        eq(resultMapper)
                );
    }

    @Test
    public void testMethodToFieldSingleton() throws Exception {

        SingletonMethodToFieldEnhancer expectedEnhancer =
                mockSingletonMethodToFieldEnhancer();

        FieldDescription.InDefinedShape singletonOriginField
                = PowerMockito.mock(FieldDescription.InDefinedShape.class,
                Answers.RETURNS_DEEP_STUBS);

        mockMethodList(0, null);
        mockFieldList(1, singletonOriginField);

        MethodDescriptor methodSingletonDescriptor
                = new TestBuilder(partialMapper, resultMapper, memberSelector).build();

        // when
        Enhancer enhancer = methodSingletonDescriptor.getEnhancer(originClass);

        // then
        assertEquals(expectedEnhancer, enhancer);
        PowerMockito
                .verifyNew(SingletonMethodToFieldEnhancer.class)
                .withArguments(
                        anyString(),
                        eq(originClass),
                        eq(singletonOriginField),
                        eq(wrapperMethod),
                        eq(resultMapper)
                );
    }

    @Test(expected = AmbiguousMethodException.class)
    public void bothMethodAndFieldMarked() throws Exception {

        FieldDescription.InDefinedShape singletonOriginField
                = PowerMockito.mock(FieldDescription.InDefinedShape.class,
                Answers.RETURNS_DEEP_STUBS);

        MethodDescription.InDefinedShape singletonOriginMethod
                = PowerMockito.mock(MethodDescription.InDefinedShape.class,
                Answers.RETURNS_DEEP_STUBS);

        mockMethodList(1, singletonOriginMethod);
        mockFieldList(1, singletonOriginField);

        MethodDescriptor methodSingletonDescriptor
                = new TestBuilder(partialMapper, resultMapper, memberSelector).build();

        methodSingletonDescriptor.getEnhancer(originClass);
    }

    @Test(expected = RuntimeException.class)
    public void neitherMethodNorFieldMarked() throws Exception {

        mockMethodList(0, null);
        mockFieldList(0, null);

        MethodDescriptor methodSingletonDescriptor
                = new TestBuilder(partialMapper, resultMapper, memberSelector).build();

        methodSingletonDescriptor.getEnhancer(originClass);
    }

    @Test(expected = AmbiguousMethodException.class)
    public void moreThanOneMethodMarked() throws Exception {

        MethodDescription.InDefinedShape singletonOriginMethod
                = PowerMockito.mock(MethodDescription.InDefinedShape.class,
                Answers.RETURNS_DEEP_STUBS);

        mockMethodList(2, singletonOriginMethod);
        mockFieldList(0, null);

        MethodDescriptor methodSingletonDescriptor
                = new TestBuilder(partialMapper, resultMapper, memberSelector).build();

        methodSingletonDescriptor.getEnhancer(originClass);
    }

    @Test(expected = AmbiguousMethodException.class)
    public void moreThanOneFieldMarked() throws Exception {

        FieldDescription.InDefinedShape singletonOriginField
                = PowerMockito.mock(FieldDescription.InDefinedShape.class,
                Answers.RETURNS_DEEP_STUBS);

        mockMethodList(0, null);
        mockFieldList(2, singletonOriginField);

        MethodDescriptor methodSingletonDescriptor
                = new TestBuilder(partialMapper, resultMapper, memberSelector).build();

        methodSingletonDescriptor.getEnhancer(originClass);
    }

    private SingletonMethodToMethodEnhancer mockSingletonMethodToMethodEnhancer()
            throws Exception {
        SingletonMethodToMethodEnhancer expectedEnhancer =
                PowerMockito.mock(SingletonMethodToMethodEnhancer.class);

        PowerMockito.mock(SingletonMethodToMethodEnhancer.class);
        PowerMockito
                .whenNew(SingletonMethodToMethodEnhancer.class)
                .withAnyArguments()
                .thenReturn(expectedEnhancer);

        return expectedEnhancer;
    }

    private SingletonMethodToFieldEnhancer mockSingletonMethodToFieldEnhancer()
            throws Exception {
        SingletonMethodToFieldEnhancer expectedEnhancer =
                PowerMockito.mock(SingletonMethodToFieldEnhancer.class);

        PowerMockito.mock(SingletonMethodToFieldEnhancer.class);
        PowerMockito
                .whenNew(SingletonMethodToFieldEnhancer.class)
                .withAnyArguments()
                .thenReturn(expectedEnhancer);

        return expectedEnhancer;
    }
}
