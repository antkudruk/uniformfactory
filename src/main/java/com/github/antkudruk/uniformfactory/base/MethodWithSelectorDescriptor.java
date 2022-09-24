package com.github.antkudruk.uniformfactory.base;

import com.github.antkudruk.uniformfactory.base.exception.NoMarkerAnnotationException;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.MemberSelector;
import lombok.Getter;

import java.lang.reflect.Method;

public abstract class MethodWithSelectorDescriptor extends AbstractMethodDescriptorImpl {
    @Getter
    protected final MemberSelector memberSelector;

    public MethodWithSelectorDescriptor(
            Method wrapperMethod,
            MemberSelector memberSelector) {
        super(wrapperMethod);
        this.memberSelector = memberSelector;
        validate();
    }

    private void validate() {
        if (memberSelector == null) {
            throw new NoMarkerAnnotationException();
        }
    }
}
