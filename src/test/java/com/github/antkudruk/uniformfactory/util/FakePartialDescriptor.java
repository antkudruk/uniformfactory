package com.github.antkudruk.uniformfactory.util;

import com.github.antkudruk.uniformfactory.singleton.argument.partialbinding.PartialDescriptor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.bytebuddy.implementation.MethodCall;

@AllArgsConstructor
public class FakePartialDescriptor<T> implements PartialDescriptor {
    @Getter
    private final int originIndex;
    private final T constant;

    @Override
    public MethodCall addWith(MethodCall methodCall) {
        return methodCall.with(constant);
    }
}
