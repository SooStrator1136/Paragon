package com.paragon.client.systems.module.hud.impl;

import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.impl.client.Colours;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

public class FPS extends HUDModule {

    public static FPS INSTANCE;

    public FPS() {
        super("FPS", "Renders your FPS on screen");

        INSTANCE = this;
    }

    @Override
    public void render() {
        renderText(getText(), getX(), getY(), Colours.mainColour.getValue().getRGB());
    }

    @Override
    public float getWidth() {
        return getStringWidth(getText());
    }

    @Override
    public float getHeight() {
        return getFontHeight();
    }

    public String getText() {
        return "FPS " + TextFormatting.WHITE + Minecraft.getDebugFPS();
    }
}
