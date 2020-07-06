package com.github.antkudruk.uniformfactory.singleton.argument.partialbinding;

import com.github.antkudruk.uniformfactory.singleton.argument.exceptions.ParameterTranslatorNotFound;
import net.bytebuddy.description.method.MethodDescription;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class PartialParameterUnionTest {

    private static class FakePartialMapper implements PartialMapper {

        private final List<PartialDescriptor> descriptors;

        @SuppressWarnings("WeakerAccess")
        public FakePartialMapper(List<PartialDescriptor> descriptors) {
            this.descriptors = descriptors;
        }

        @Override
        public List<PartialDescriptor> getArgumentBinders(MethodDescription originMethod) {
            return this.descriptors;
        }
    }

    @Test
    public void singleTest() throws Exception {

        MethodDescription method = new MethodDescription.ForLoadedMethod(
                getClass().getMethod("methodWith3Parameters",
                        int.class, int.class, int.class));

        PartialParameterDescriptor<Object, Object> firstDescriptor = new PartialParameterDescriptor<>(0, 1, t -> t);
        PartialParameterDescriptor<Object, Object> secondDescriptor = new PartialParameterDescriptor<>(1, 0, t -> t);
        PartialParameterDescriptor<Object, Object> thirdDescriptor = new PartialParameterDescriptor<>(2, 2, t -> t);

        FakePartialMapper first = new FakePartialMapper(Arrays.asList(
                firstDescriptor, secondDescriptor, thirdDescriptor));

        PartialParameterUnion partialParameterUnion = new PartialParameterUnion(first);

        List<PartialDescriptor> binders = partialParameterUnion.getParameterBinders(method);

        assertEquals(firstDescriptor, binders.get(0));
        assertEquals(secondDescriptor, binders.get(1));
        assertEquals(thirdDescriptor, binders.get(2));
    }

    @Test
    public void overlappingTest() throws Exception {

        MethodDescription method = new MethodDescription.ForLoadedMethod(
                getClass().getMethod("methodWith3Parameters",
                        int.class, int.class, int.class));

        PartialParameterDescriptor<?, ?> firstDescriptor0 = new PartialParameterDescriptor<>(0, 1, t -> t);
        PartialParameterDescriptor<Object, Object> firstDescriptor1 = new PartialParameterDescriptor<>(1, 0, t -> t);

        FakePartialMapper first = new FakePartialMapper(Arrays.asList(
                firstDescriptor0, firstDescriptor1));

        PartialParameterDescriptor<Object, Object> secondDescriptor0 = new PartialParameterDescriptor<>(2, 0, t -> t);
        PartialParameterDescriptor<Object, Object> secondDescriptor1 = new PartialParameterDescriptor<>(1, 1, t -> t);

        FakePartialMapper second = new FakePartialMapper(Arrays.asList(
                secondDescriptor0, secondDescriptor1));

        PartialParameterUnion partialParameterUnion = new PartialParameterUnion(first, second);

        List<PartialDescriptor> binders = partialParameterUnion.getParameterBinders(method);

        assertEquals(firstDescriptor0, binders.get(0));
        assertEquals(secondDescriptor1, binders.get(1));
        assertEquals(secondDescriptor0, binders.get(2));
    }

    @Test(expected = ParameterTranslatorNotFound.class)
    public void missingBinderTest() throws Exception {

        MethodDescription method = new MethodDescription.ForLoadedMethod(
                getClass().getMethod("methodWith3Parameters",
                        int.class, int.class, int.class));

        FakePartialMapper first = new FakePartialMapper(Collections.singletonList(
                new PartialParameterDescriptor<>(0, 1, t -> t)));

        FakePartialMapper second = new FakePartialMapper(Collections.singletonList(
                new PartialParameterDescriptor<>(2, 1, t -> t)));

        PartialParameterUnion partialParameterUnion = new PartialParameterUnion(first, second);

        partialParameterUnion.getParameterBinders(method);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public void methodWith3Parameters(int first, int second, int third) {

    }
}
