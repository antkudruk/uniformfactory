package com.github.antkudruk.uniformfactory.test.gradleplugin.classgenerationsingleton;

import com.github.antkudruk.uniformfactory.test.gradleplugin.classgenerationsingleton.origin.Origin;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Proofs that {@code MetaClassFactory} is a singleton for each plugin.
 * Proofs that {@code MetaClassFactory.generateMetaClass} returns singleton
 * for each class.
 */
public class ClassGenerationSingletonTest {

    @Origin.Marker
    private static class FirstClass {

    }

    @Origin.Marker
    private static class SecondClass {

    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void test() {
        FirstClass first0 = new FirstClass();
        assertEquals(1, ((Origin)first0).getWrapper().getClassNumber());
        assertEquals(1, ((Origin)first0).getWrapper().getObjectNumber());

        FirstClass first1 = new FirstClass();
        assertEquals(1, ((Origin)first1).getWrapper().getClassNumber());
        assertEquals(2, ((Origin)first1).getWrapper().getObjectNumber());

        SecondClass second0 = new SecondClass();
        assertEquals(2, ((Origin)second0).getWrapper().getClassNumber());
        assertEquals(1, ((Origin)second0).getWrapper().getObjectNumber());

        SecondClass second1 = new SecondClass();
        assertEquals(2, ((Origin)second1).getWrapper().getClassNumber());
        assertEquals(2, ((Origin)second1).getWrapper().getObjectNumber());

        FirstClass first2 = new FirstClass();
        assertEquals(1, ((Origin)first2).getWrapper().getClassNumber());
        assertEquals(3, ((Origin)first2).getWrapper().getObjectNumber());

        SecondClass second3 = new SecondClass();
        assertEquals(2, ((Origin)second3).getWrapper().getClassNumber());
        assertEquals(3, ((Origin)second3).getWrapper().getObjectNumber());
    }
}
