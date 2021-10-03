package com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.tree.domain;

import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.tree.Label;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.tree.Nested;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.tree.TreeElementMarker;

import java.util.Arrays;
import java.util.List;

@TreeElementMarker
public class Company {

    @Label
    private static final String companyName = "My awesome company";

    @Nested
    public List<Department> getDepartments() {
        return Arrays.asList(
                new Department("Managers", "Beavis", "Butthead"),
                new Department("Labours", "Stewart")
        );
    }
}
