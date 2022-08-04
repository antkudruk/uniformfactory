package com.github.antkudruk.uniformfactory.methodcollection.getterelementfactory;

import com.github.antkudruk.uniformfactory.methodcollection.GetterElementFactoryForElementTypeTest;

public class GetterElementFactoryReturningPrimitiveTest extends
        GetterElementFactoryForElementTypeTest<GetterElementFactoryReturningPrimitiveTest.FunReturningPrimitive, Integer> {

    public GetterElementFactoryReturningPrimitiveTest() {
        super(FunReturningPrimitive.class, Integer.class, 3);
    }

    @Override
    protected Integer callMethod(FunReturningPrimitive element) {
            return element.get();
    }

    public interface FunReturningPrimitive {
        int get();
    }
}
