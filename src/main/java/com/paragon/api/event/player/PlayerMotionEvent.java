package com.paragon.api.event.player;

import me.wolfsurge.cerauno.event.CancellableEvent;
import net.minecraft.entity.MoverType;

public class PlayerMotionEvent extends CancellableEvent {
    private MoverType type;
    private double x;
    private double y;
    private double z;

    public PlayerMotionEvent(MoverType type, double x, double y, double z) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MoverType getType() {
        return type;
    }

    public void setType(MoverType in) {
        type = in;
    }

    public double getX() {
        return x;
    }

    public void setX(double in) {
        x = in;
    }

    public double getY() {
        return y;
    }

    public void setY(double in) {
        y = in;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double in) {
        z = in;
    }
}
