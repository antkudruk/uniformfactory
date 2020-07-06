package com.github.antkudruk.uniformfactory.test.gradleplugin.calendar.origins;

import com.github.antkudruk.uniformfactory.test.gradleplugin.calendar.IsTimeInterval;
import com.github.antkudruk.uniformfactory.test.gradleplugin.calendar.Nested;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
@IsTimeInterval
public class Hour {
    @SuppressWarnings("unused")
    @Nested
    final List nested = new ArrayList();
}
