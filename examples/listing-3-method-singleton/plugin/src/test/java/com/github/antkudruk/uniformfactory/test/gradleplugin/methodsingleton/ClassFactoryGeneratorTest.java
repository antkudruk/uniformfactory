package com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton;

import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.domain.OriginWithBoxedLongFieldIdentity;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.domain.OriginWithBoxedLongMethodIdentity;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.domain.OriginWithUnboxedLongFieldIdentity;
import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class ClassFactoryGeneratorTest {
    private ClassFactoryGeneratorImpl testSubject = new ClassFactoryGeneratorImpl();

    public ClassFactoryGeneratorTest() throws NoSuchMethodException {
    }

    @Test
    public void givenBoxedLongField_whenGenerateMetaClass_thenGenerateWrapper() {
        // given
        OriginWithBoxedLongFieldIdentity origin = new OriginWithBoxedLongFieldIdentity();

        // when
        Function<OriginWithBoxedLongFieldIdentity, ? extends Wrapper> adapterFactory = testSubject
                .generateMetaClass(OriginWithBoxedLongFieldIdentity.class);

        // then
        Wrapper adapter = adapterFactory.apply(origin);
        assertEquals("10", adapter.getIdentity());
    }

    @Test
    public void givenUnboxedLongField_whenGenerateMetaClass_thenGenerateWrapper() {
        // given
        OriginWithUnboxedLongFieldIdentity origin = new OriginWithUnboxedLongFieldIdentity();

        // when
        Function<OriginWithUnboxedLongFieldIdentity, ? extends Wrapper> adapterFactory = testSubject
                .generateMetaClass(OriginWithUnboxedLongFieldIdentity.class);

        // then
        Wrapper adapter = adapterFactory.apply(origin);
        assertEquals("20", adapter.getIdentity());
    }

    @Test
    public void givenUnboxedLongMethod_whenGenerateMetaClass_thenGenerateWrapper() {
        // given
        OriginWithBoxedLongMethodIdentity origin = new OriginWithBoxedLongMethodIdentity();

        // when
        Function<OriginWithBoxedLongMethodIdentity, ? extends Wrapper> adapterFactory = testSubject
                .generateMetaClass(OriginWithBoxedLongMethodIdentity.class);

        // then
        Wrapper adapter = adapterFactory.apply(origin);
        assertEquals("30", adapter.getIdentity());
    }
}
