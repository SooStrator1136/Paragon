package com.paragon.client.systems.module.hud.impl;

import com.paragon.Paragon;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.api.setting.Setting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class Watermark extends HUDModule {

    public static Watermark INSTANCE;

    public static Setting<Display> display = new Setting<>("Display", Display.TEXT)
            .setDescription("The type of watermark to display");

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

                // Set image colour
                ColourUtil.setColour(Colours.mainColour.getValue().getRGB());

                RenderUtil.drawModalRectWithCustomSizedTexture(getX(), getY(), 0, 0, 160, 25, 160, 25);
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
