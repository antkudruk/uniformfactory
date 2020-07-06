package com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist.domain;

import com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist.Marker;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist.Processor;

import java.util.function.Function;

@SuppressWarnings("unused")
@Marker
public class Origin2 {

    private final Function<String, String> consumerString;
    private final Function<Integer, Boolean> consumerInteger;

    public Origin2(Function<String, String> consumerString, Function<Integer, Boolean> consumerInteger) {
        this.consumerString = consumerString;
        this.consumerInteger = consumerInteger;
    }

    @Processor.Process
    public String processString(String event) {
        return consumerString.apply(event);
    }

    @Processor.Process
    public Boolean processInteger(Integer event) {
        return consumerInteger.apply(event);
    }
}
