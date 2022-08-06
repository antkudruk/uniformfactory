package com.github.antkudruk.uniformfactory.methodcollection.seletor;

import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.type.TypeDescription;

import java.util.Collections;
import java.util.List;

// TODO: Add test
public class SpecifiedFieldSelector implements MemberSelector {

    private final FieldDescription fieldDescription;

    public SpecifiedFieldSelector(FieldDescription fieldDescription) {
        this.fieldDescription = fieldDescription;
    }

    @Override
    public List<FieldDescription> getFields(TypeDescription type) {
        return Collections.singletonList(fieldDescription);
    }
}
