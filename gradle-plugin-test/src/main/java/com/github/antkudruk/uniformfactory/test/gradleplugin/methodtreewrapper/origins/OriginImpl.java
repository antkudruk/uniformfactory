package com.github.antkudruk.uniformfactory.test.gradleplugin.methodtreewrapper.origins;

import com.github.antkudruk.uniformfactory.test.gradleplugin.methodtreewrapper.First;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodtreewrapper.FunctionalElement;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodtreewrapper.HasWrapper;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodtreewrapper.Second;

@SuppressWarnings("unused")
@HasWrapper
public class OriginImpl {

    @FunctionalElement("alpha")
    public String fieldValue = "Alpha Value";

    @FunctionalElement("epsilon")
    public String staticFieldValue = "Epsilon Value";


    @FunctionalElement("beta")
    public String get( ) {
        return "Beta Value";
    }

    @FunctionalElement("gamma")
    public String get(@First String first, @Second Long second) {
        return first + " " + second + " units";
    }

    @FunctionalElement("delta")
    public String get(@First String first, @Second Boolean second) {
        return first + " " + (second ? "Yes" : "No");
    }
}
