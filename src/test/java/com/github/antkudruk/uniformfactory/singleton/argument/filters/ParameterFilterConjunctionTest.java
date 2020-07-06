package com.github.antkudruk.uniformfactory.singleton.argument.filters;

import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.ParameterFilterConjunction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParameterFilterConjunctionTest extends ParameterFilterBaseTest {

    @Test
    public void bothTrue() {
        test(true, true, true);
    }

    @Test
    public void trueAndFalse() {
        test(true, false, false);
    }

    @Test
    public void falseAnsTrue() {
        test(false, true, false);
    }

    @Test
    public void bothFalse() {
        test(false, false, false);
    }

    private void test(boolean first, boolean second, boolean expected) {

        ParameterFilter firstParameterFilter = mockParameterFilter(first);
        ParameterFilter secondParameterFilter = mockParameterFilter(second);

        assertEquals(first, firstParameterFilter.useArgument(null, 0));
        assertEquals(second, secondParameterFilter.useArgument(null, 0));

        ParameterFilterConjunction conjunction = new ParameterFilterConjunction();

        assertEquals(expected, conjunction
                .and(firstParameterFilter)
                .and(secondParameterFilter)
                .useArgument(null, 0));
    }
}
