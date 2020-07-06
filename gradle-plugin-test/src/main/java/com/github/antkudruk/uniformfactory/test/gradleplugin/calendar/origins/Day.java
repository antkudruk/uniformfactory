package com.github.antkudruk.uniformfactory.test.gradleplugin.calendar.origins;

import com.github.antkudruk.uniformfactory.test.gradleplugin.calendar.IsTimeInterval;
import com.github.antkudruk.uniformfactory.test.gradleplugin.calendar.Nested;

import java.util.Arrays;
import java.util.List;

@IsTimeInterval
public class Day {
    @Nested
    public List<Hour> hours = Arrays.asList(
            new Hour(),
            new Hour(),
            new Hour(),
            new Hour());
}
