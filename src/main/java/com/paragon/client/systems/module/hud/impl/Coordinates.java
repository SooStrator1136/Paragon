package com.paragon.client.systems.module.hud.impl;

import com.paragon.api.util.render.font.FontUtil;
import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.impl.client.Colours;
import net.minecraft.util.text.TextFormatting;

public class Coordinates extends HUDModule {

    public static Coordinates INSTANCE;

    public Coordinates() {
        super("Coordinates", "Displays your coordinates");

        INSTANCE = this;
    }

    @Override
    public void render() {
        FontUtil.drawStringWithShadow(getText(), getX(), getY(), Colours.mainColour.getValue().getRGB());
    }

    @Override
    public float getWidth() {
        return FontUtil.getStringWidth(getText());
    }

    @Override
    public float getHeight() {
        return FontUtil.getHeight();
    }

    public String getText() {
        return "X " + TextFormatting.WHITE + Math.round(mc.player.posX) + TextFormatting.RESET + " Y " + TextFormatting.WHITE + Math.round(mc.player.posY) + TextFormatting.RESET + " Z " + TextFormatting.WHITE + Math.round(mc.player.posZ);
    }

}
