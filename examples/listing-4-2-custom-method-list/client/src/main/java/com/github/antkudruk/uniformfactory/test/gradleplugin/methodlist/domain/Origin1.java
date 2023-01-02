package com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist.domain;

import com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist.CssProperty;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist.Marker;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist.Processor;

@SuppressWarnings("unused")
@Marker
public class Origin1 {

    private final Runnable runnable;

    @CssProperty
    private int fontSize;
    @CssProperty
    private Long width;
    @CssProperty
    private String fontName;

    public Origin1(Runnable runnable) {
        this.runnable = runnable;
    }

    @Processor.Process
    public void process() {
        runnable.run();
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public int getFontSize() {
        return fontSize;
    }

    public Long getWidth() {
        return width;
    }


    public String getFontName() {
        return fontName;
    }
}
