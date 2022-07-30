package com.paragon.client.systems.module.hud.impl;

import com.paragon.Paragon;
import com.paragon.api.setting.Setting;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.impl.client.Colours;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import static org.lwjgl.opengl.GL11.*;

public class Watermark extends HUDModule {

    public static Watermark INSTANCE;

    private static Setting<Display> display = new Setting<>("Display", Display.TEXT)
            .setDescription("The type of watermark to display");

    private static Setting<Double> scaleFac = new Setting<>("Size", 1.0, 0.1, 2.0, 0.05)
            .setDescription("The scale of the image watermark")
            .setVisibility(() -> display.getValue().equals(Display.IMAGE));

    public Watermark() {
        super("Watermark", "Renders the client's name on screen");

        INSTANCE = this;
    }

    @Override
    public void render() {
        switch (display.getValue()) {
            case TEXT:
                renderText("Paragon " + TextFormatting.GRAY + Paragon.modVersion, getX(), getY(), Colours.mainColour.getValue().getRGB());
                break;

            case IMAGE:
                mc.getTextureManager().bindTexture(new ResourceLocation("paragon", "textures/paragon.png"));

                glPushMatrix();
                float width = 880 / 4f;
                float height = 331 / 4f;
                glTranslatef(getX(), getY(), 0F);
                glScaled(scaleFac.getValue(), scaleFac.getValue(), 1.0);
                glTranslatef(-getX(), -getY(), 0F);

                RenderUtil.drawModalRectWithCustomSizedTexture(getX(), getY(), 0, 0, width, height, width, height);
                glPopMatrix();
                break;
        }
    }

    @Override
    public float getWidth() {
        return display.getValue().getWidth();
    }

    @Override
    public float getHeight() {
        return display.getValue().getHeight();
    }

    public enum Display {
        /**
         * Watermark will be text
         */
        TEXT(mc.fontRenderer.getStringWidth("Paragon " + Paragon.modVersion), 12),

        /**
         * Watermark will be an image
         */
        IMAGE(160, 25);

        private final float width;
        private final float height;

        Display(float width, float height) {
            this.width = width;
            this.height = height;
        }

        public float getWidth() {
            return width;
        }

        public float getHeight() {
            return height;
        }
    }
}
