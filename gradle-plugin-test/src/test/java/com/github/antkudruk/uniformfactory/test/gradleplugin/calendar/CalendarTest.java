package com.github.antkudruk.uniformfactory.test.gradleplugin.calendar;

import com.github.antkudruk.uniformfactory.test.gradleplugin.calendar.origins.Week;
import com.github.antkudruk.uniformfactory.test.gradleplugin.calendar.wrapper.HasInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CalendarTest {
    @SuppressWarnings("ConstantConditions")
    @Test
    public void test() {
        Week week = new Week();

        assertEquals(((HasInterval) week).getInterval()
                        .getNested().get(2).getInterval()
                        .getNested().get(1).getInterval(),
                ((HasInterval) week.days.get(2).hours.get(1)).getInterval());
    }
}
