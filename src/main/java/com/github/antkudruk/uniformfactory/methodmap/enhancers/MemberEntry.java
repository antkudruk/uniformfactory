package com.github.antkudruk.uniformfactory.methodmap.enhancers;

import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.bytecode.StackManipulation;

// TODO: Add some type safity to Unloaded
public class MemberEntry {
    private final StackManipulation key;
    private final DynamicType.Unloaded value;

    public MemberEntry(StackManipulation key, DynamicType.Unloaded value) {
        this.key = key;
        this.value = value;
    }

    public StackManipulation getKey() {
        return key;
    }

    public DynamicType.Unloaded getValue() {
        return value;
    }
}
