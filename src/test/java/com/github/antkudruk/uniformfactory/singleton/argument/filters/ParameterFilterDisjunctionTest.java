package com.github.antkudruk.uniformfactory.singleton.argument.filters;

import com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes.ParameterFilterDisjunction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParameterFilterDisjunctionTest extends ParameterFilterBaseTest {

    @Test
    public void bothTrue() {
        test(true, true, true);
    }

    @Test
    public void trueAndFalse() {
        test(true, false, true);
    }

    @Test
    public void falseAnsTrue() {
        test(false, true, true);
    }

    @Test
    public void bothFalse() {
        test(false, false, false);
    }

    private void test(boolean first, boolean second, boolean expected) {

        ParameterFilter firstParameterFilter = mockParameterFilter(first);
        ParameterFilter secondParameterFilter = mockParameterFilter(second);

        ParameterFilterDisjunction disjunction = new ParameterFilterDisjunction();

        assertEquals(expected, disjunction
                .or(firstParameterFilter)
                .or(secondParameterFilter)
                .useArgument(null, 0));
    }
}
