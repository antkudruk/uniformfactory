package com.github.antkudruk.uniformfactory.methodcollection.getterelementfactory;

import com.github.antkudruk.uniformfactory.methodcollection.GetterElementFactoryForElementTypeTest;

public class GetterElementFactoryReturningVoidTest extends
        GetterElementFactoryForElementTypeTest<GetterElementFactoryReturningVoidTest.FunReturningVoid, Void> {

    public GetterElementFactoryReturningVoidTest() {
        super(FunReturningVoid.class, Void.class, null);
    }

    @Override
    protected Void callMethod(FunReturningVoid element) {
        element.get();
        return null;
    }

    public interface FunReturningVoid {
        void get();
    }
}
