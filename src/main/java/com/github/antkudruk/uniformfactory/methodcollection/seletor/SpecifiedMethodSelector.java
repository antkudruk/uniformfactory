package com.github.antkudruk.uniformfactory.methodcollection.seletor;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;

import java.util.Collections;
import java.util.List;

// TODO: Add test
public class SpecifiedMethodSelector implements MemberSelector {

    private final MethodDescription methodDescription;

    public SpecifiedMethodSelector(MethodDescription methodDescription) {
        this.methodDescription = methodDescription;
    }

    @Override
    public List<MethodDescription> getMethods(TypeDescription type) {
        return Collections.singletonList(methodDescription);
    }
}
