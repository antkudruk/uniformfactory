package com.github.antkudruk.uniformfactory.methodcollection.getterelementfactory;

import com.github.antkudruk.uniformfactory.methodcollection.GetterElementFactoryForElementTypeTest;

public class GetterElementFactoryReturningStringTest extends
        GetterElementFactoryForElementTypeTest<GetterElementFactoryReturningStringTest.FunReturningString, String> {

    public static final String CORRECT_STRING_RESULT = "Correct string result";

    public GetterElementFactoryReturningStringTest() {
        super(
                GetterElementFactoryReturningStringTest.FunReturningString.class,
                String.class,
                CORRECT_STRING_RESULT);
    }

    @Override
    protected String callMethod(FunReturningString element) {
        return element.get();
    }

    public interface FunReturningString {
        String get();
    }
}
