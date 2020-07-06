package com.github.antkudruk.uniformfactory.test.gradleplugin.calendar.origins;

import com.github.antkudruk.uniformfactory.test.gradleplugin.calendar.IsTimeInterval;
import com.github.antkudruk.uniformfactory.test.gradleplugin.calendar.Nested;

import java.util.Arrays;
import java.util.List;

@IsTimeInterval
public class Week {
    @Nested
    public List<Day> days = Arrays.asList(
            new Day(),
            new Day(),
            new Day(),
            new Day(),
            new Day());
}
