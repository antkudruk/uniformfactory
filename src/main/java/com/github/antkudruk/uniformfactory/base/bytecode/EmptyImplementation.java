package com.github.antkudruk.uniformfactory.base.bytecode;

import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;

// TODO: Replace with standard byteBuddy composable (something like doNothing)
public class EmptyImplementation extends AbstractTerminatableImplementation {
    public EmptyImplementation() {
        super(true);
    }

    private EmptyImplementation(boolean terminate) {
        super(terminate);
    }

    @Override
    protected AbstractTerminatableImplementation cloneNotTerminated() {
        return new EmptyImplementation(false);
    }

    @Override
    public ByteCodeAppender appender(Target implementationTarget) {
        return (methodVisitor, implementationContext, instrumentedMethod) -> new ByteCodeAppender.Size(new StackManipulation.Compound(
                isTerminating() ? MethodReturn.VOID : StackManipulation.Trivial.INSTANCE
        ).apply(methodVisitor, implementationContext)
                .getMaximalSize(),
                instrumentedMethod.getStackSize());
    }

    @Override
    public InstrumentedType prepare(InstrumentedType instrumentedType) {
        return instrumentedType;
    }
}
