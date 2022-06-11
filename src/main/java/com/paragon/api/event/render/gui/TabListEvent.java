package com.paragon.api.event.render.gui;

import me.wolfsurge.cerauno.event.CancellableEvent;

public class TabListEvent extends CancellableEvent {

    private float size;

    public TabListEvent(float size) {
        this.size = size;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

}
