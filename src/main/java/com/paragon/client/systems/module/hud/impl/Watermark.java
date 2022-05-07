package com.paragon.client.systems.module.hud.impl;

import com.paragon.Paragon;
import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.impl.client.Colours;
import net.minecraft.util.text.TextFormatting;

public class Watermark extends HUDModule {

    public Watermark() {
        super("Watermark", "Renders the client's name on screen");
    }

    @Override
    public void render() {
        renderText("Paragon " + TextFormatting.GRAY + Paragon.modVersion, getX(), getY(), Colours.mainColour.getValue().getRGB());
    }

    @Override
    public float getWidth() {
        return getStringWidth("Paragon " + TextFormatting.GRAY + Paragon.modVersion);
    }

    @Override
    public float getHeight() {
        return getFontHeight();
    }
}
