package com.github.antkudruk.uniformfactory.base;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IdentifierGeneratorImpl implements IdentifierGenerator {

    // TODO: Refactor existing objects to use these generators
    public static final IdentifierGeneratorImpl CONSTANT_ID_GENERATOR = new IdentifierGeneratorImpl("UNIFORM_FACTORY_CONSTANT");
    public static final IdentifierGeneratorImpl FIELD_ID_GENERATOR = new IdentifierGeneratorImpl("uniformFactoryField");
    public static final IdentifierGeneratorImpl METHOD_ID_GENERATOR = new IdentifierGeneratorImpl("uniformFactoryMethod");

    private final String prefix;
    private int index = 1;

    @Override
    public String next() {
        return String.format("%s%08X", prefix, index++);
    }
}
