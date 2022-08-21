package com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.tree.domain;

import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.tree.Label;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.tree.Nested;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.tree.TreeElementMarker;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
@TreeElementMarker
public class Department {

    @Label
    private final String depName;

    @Nested
    private final List<Employee> employee;

    public Department(String depName, String... employee) {
        this.depName = depName;
        this.employee = Stream.of(employee)
                .map(Employee::new)
                .collect(Collectors.toList());
    }
}
