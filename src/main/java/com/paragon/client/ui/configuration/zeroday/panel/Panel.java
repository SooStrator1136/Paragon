package com.paragon.client.ui.configuration.zeroday.panel;

import com.paragon.client.ui.configuration.zeroday.element.Element;

public abstract class Panel extends Element {

    private float lastX;
    private float lastY;

    public Panel(float x, float y, float width, float height) {
        super(0, x, y, width, height);
    }

    public float getLastX() {
        return lastX;
    }

    public void setLastX(float lastX) {
        this.lastX = lastX;
    }

    public float getLastY() {
        return lastY;
    }

    public void setLastY(float lastY) {
        this.lastY = lastY;
    }

}
