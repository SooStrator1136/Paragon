package com.paragon.api.event.render;

import me.wolfsurge.cerauno.event.CancellableEvent;

public class AspectEvent extends CancellableEvent {

    private float ratio;

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

}
