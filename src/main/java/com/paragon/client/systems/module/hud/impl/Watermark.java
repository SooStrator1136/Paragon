package com.paragon.client.systems.module.hud.impl;

import com.paragon.Paragon;
import com.paragon.api.setting.Setting;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.font.FontUtil;
import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.impl.client.Colours;
import kotlin.Unit;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import static com.paragon.api.util.render.RenderUtil.scaleTo;

public final class Watermark extends HUDModule {

    public static Watermark INSTANCE;

    private static Setting<Display> display = new Setting<>("Display", Display.TEXT)
            .setDescription("The type of watermark to display");

    private static Setting<Double> scaleFac = new Setting<>("Size", 1.0, 0.1, 2.0, 0.05)
            .setDescription("The scale of the image watermark")
            .setVisibility(() -> display.getValue().equals(Display.IMAGE));

    private final ResourceLocation icon = new ResourceLocation("paragon", "textures/paragon.png");

    public Watermark() {
        super("Watermark", "Renders the client's name on screen");

        INSTANCE = this;
    }

    @Override
    public void render() {
        switch (display.getValue()) {
            case TEXT:
                FontUtil.drawStringWithShadow("Paragon " + TextFormatting.GRAY + Paragon.modVersion, getX(), getY(), Colours.mainColour.getValue().getRGB());
                break;

            case IMAGE:
                mc.getTextureManager().bindTexture(this.icon);

                final float width = 880 / 4.0F;
                final float height = 331 / 4.0F;
                scaleTo(this.getX(), this.getY(), 0.0F, scaleFac.getValue(), scaleFac.getValue(), 1.0D, unit -> {
                    RenderUtil.drawModalRectWithCustomSizedTexture(this.getX(), this.getY(), 0, 0, width, height, width, height);
                    return Unit.INSTANCE;
                });
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
