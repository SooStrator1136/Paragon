package com.paragon.client.systems.ui.window.impl.windows;

import com.paragon.api.util.Wrapper;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.ui.window.impl.Window;

public abstract class Component implements TextRenderer, Wrapper {

    private final Window window;

    private float x;
    private float y;
    private float width;
    private float height;

    public Component(Window window, float x, float y, float width, float height) {
        this.window = window;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void drawComponent(int mouseX, int mouseY);
    public abstract void mouseClicked(int mouseX, int mouseY, int button);
    public abstract void mouseReleased(int mouseX, int mouseY, int button);
    public abstract void keyTyped(char typedChar, int keyCode);

    public Window getWindow() {
        return window;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return isHovered(getX(), getY(), getWidth(), getHeight(), mouseX, mouseY);
    }

    public boolean isWithinWindowBounds(float minY, float maxY) {
        return getY() >= minY && getY() + getHeight() <= maxY;
    }

}
