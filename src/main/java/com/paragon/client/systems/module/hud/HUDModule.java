package com.paragon.client.systems.module.hud;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class HUDModule extends Module implements TextRenderer {

    private float x = 50, y = 50;
    private float width = 50, height = 50;
    private float lastX, lastY;
    private boolean dragging;

    public HUDModule(String name, String description) {
        super(name, Category.HUD, description);
        this.setVisible(false);
    }

    public abstract void render();

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void updateComponent(int mouseX, int mouseY) {
        // Set X and Y
        if (dragging) {
            ScaledResolution sr = new ScaledResolution(mc);

            float newX = MathHelper.clamp(mouseX - lastX, 2, RenderUtil.getScreenWidth() - getWidth());
            float newY = MathHelper.clamp(mouseY - lastY, 2, RenderUtil.getScreenHeight() - getHeight());

            this.x = newX;
            this.y = newY;

            float centerX = newX + (getWidth() / 2f);
            float centerY = newY + (getHeight() / 2f);

            if (centerX > (sr.getScaledWidth() / 2f) - 5 && centerX < (sr.getScaledWidth() / 2f) + 5) {
                this.x = (sr.getScaledWidth() / 2f) - (getWidth() / 2f);
            }

            if (centerY > (sr.getScaledHeight() / 2f) - 5 && centerY < (sr.getScaledHeight() / 2f) + 5) {
                this.y = (sr.getScaledHeight() / 2f) - (getHeight() / 2f);
            }
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovered(getX(), getY(), getWidth(), getHeight(), mouseX, mouseY)) {
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

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public boolean isDragging() {
        return dragging;
    }

}
