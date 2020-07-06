package com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist;

import java.util.Collection;
import java.util.WeakHashMap;

public class CallableObjectsRegistry {

    public static final CallableObjectsRegistry INSTANCE = new CallableObjectsRegistry();

    private final WeakHashMap<Wrapper, Object> object = new WeakHashMap<>();

    void addObject(Wrapper wrapper) {
        object.put(wrapper, null);
    }

    public boolean call(String eventName) {
        return object.keySet().stream()
                .map(Wrapper::getProcessors)
                .flatMap(Collection::stream)
                .map(t -> t.process(eventName))
                .reduce(true, (a, b) -> a & b );
    }
}
