package com.paragon.api.event.render.entity;

import me.wolfsurge.cerauno.event.CancellableEvent;

public class CameraClipEvent extends CancellableEvent {

    private double distance;

    public CameraClipEvent(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

}
