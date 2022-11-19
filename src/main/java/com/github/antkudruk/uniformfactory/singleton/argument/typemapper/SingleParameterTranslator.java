package com.github.antkudruk.uniformfactory.singleton.argument.typemapper;

import net.bytebuddy.description.type.TypeDescription;

import java.util.function.Function;

public interface SingleParameterTranslator<A> {
    boolean isApplicable(TypeDescription originParameterClass);
    Function<A, ?> getTranslator();
}
