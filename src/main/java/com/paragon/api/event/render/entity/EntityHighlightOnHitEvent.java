package com.paragon.api.event.render.entity;

import me.wolfsurge.cerauno.event.CancellableEvent;

import java.awt.*;

public class EntityHighlightOnHitEvent extends CancellableEvent {

    private Color colour;

    public Color getColour() {
        return colour;
    }

    public void setColour(Color newColour) {
        this.colour = newColour;
    }

}
