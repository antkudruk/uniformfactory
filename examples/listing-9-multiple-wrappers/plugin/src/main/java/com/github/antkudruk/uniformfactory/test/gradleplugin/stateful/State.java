package com.github.antkudruk.uniformfactory.test.gradleplugin.stateful;

public class State {

    private final Origin origin;

    private Integer previousId;

    public State(Origin origin) {
        this.origin = origin;
    }

    public Integer getId() {
        Integer previousId = this.previousId;
        this.previousId = this.origin.getAdapter().getValue();
        return previousId;
    }

}
