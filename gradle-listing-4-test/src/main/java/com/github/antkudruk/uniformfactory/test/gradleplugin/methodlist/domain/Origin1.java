package com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist.domain;

import com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist.Marker;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist.Processor;

@SuppressWarnings("unused")
@Marker
public class Origin1 {

    private final Runnable runnable;

    public Origin1(Runnable runnable) {
        this.runnable = runnable;
    }

    @Processor.Process
    public void process() {
        runnable.run();
    }
}
