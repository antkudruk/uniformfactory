package com.github.antkudruk.uniformfactory.singleton.argument.typemapper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.description.type.TypeDescription;

import java.util.function.Function;

@RequiredArgsConstructor
public class SuperParameterTranslator<A> implements SingleParameterTranslator<A> {

    private final TypeDescription originParameterClass;

    @Getter
    private final Function<A, ?> translator;

    public SuperParameterTranslator(Class<?> originParameterClass, Function<A, ?> translator) {
        this(new TypeDescription.ForLoadedType(originParameterClass), translator);
    }

    @Override
    public boolean isApplicable(TypeDescription originParameterClass) {
        return this
                .originParameterClass
                .asBoxed()
                .isAssignableTo(originParameterClass.asBoxed());
    }
}
