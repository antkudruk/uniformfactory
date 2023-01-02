package com.github.antkudruk.uniformfactory.test.gradleplugin.methodlist;

import java.util.List;

public interface Wrapper {
    List<Processor> getProcessors();
    List<CssPropertySetter> getDescriptors();
}
