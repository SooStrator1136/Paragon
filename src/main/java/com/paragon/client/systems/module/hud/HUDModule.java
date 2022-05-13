package com.paragon.client.systems.module.hud;

import com.paragon.api.util.render.GuiUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import net.minecraft.util.math.MathHelper;

public abstract class HUDModule extends Module implements TextRenderer {

    private float x = 50, y = 50;
    private float lastX, lastY;
    private boolean dragging;

    public HUDModule(String name, String description) {
        super(name, Category.HUD, description);
        this.setVisible(false);
    }

    public abstract void render();

    public abstract float getWidth();
    public abstract float getHeight();

    public void updateComponent(int mouseX, int mouseY) {
        // Set X and Y
        if (dragging) {
            this.x = MathHelper.clamp(mouseX - lastX, 2, RenderUtil.getScreenWidth());
            this.y = MathHelper.clamp(mouseY - lastY, 2, RenderUtil.getScreenHeight() - getHeight());
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (GuiUtil.mouseOver(getX(), getY(), getX() + getWidth(), getY() + getHeight(), mouseX, mouseY)) {
            if (mouseButton == 0) {
                this.lastX = mouseX - getX();
                this.lastY = mouseY - getY();

                dragging = true;
            } else if (mouseButton == 1) {
                if (this.isEnabled()) {
                    toggle();
                }
            }
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        dragging = false;
    }

    public float getX() {
        return x;
    }

    public void setX(float newX) {
        this.x = newX;
    }

    public float getY() {
        return y;
    }

    public void setY(float newY) {
        this.y = newY;
    }

}
