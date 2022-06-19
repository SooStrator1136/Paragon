package com.paragon.api.event.render.gui;

import me.wolfsurge.cerauno.event.CancellableEvent;

public class RenderChatEvent extends CancellableEvent {

    private int colour;

    public RenderChatEvent(int colour) {
        this.colour = colour;
    }

    public int getColour() {
        return colour;
    }

    public void setColour(int colour) {
        this.colour = colour;
    }

}
