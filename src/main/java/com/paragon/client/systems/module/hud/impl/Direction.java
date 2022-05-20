package com.paragon.client.systems.module.hud.impl;

import com.paragon.api.util.player.PlayerUtil;
import com.paragon.api.util.string.EnumFormatter;
import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.impl.client.Colours;
import net.minecraft.util.text.TextFormatting;

public class Direction extends HUDModule {

    public static Direction INSTANCE;

    public Direction() {
        super("Direction", "Displays what direction you are facing");

        INSTANCE = this;
    }

    @Override
    public void render() {
        renderText("Direction " + TextFormatting.WHITE + EnumFormatter.getFormattedText(PlayerUtil.getDirection()) + " [" + PlayerUtil.getAxis(PlayerUtil.getDirection()) + "]", getX(), getY(), Colours.mainColour.getValue().getRGB());
    }

    @Override
    public float getWidth() {
        return getStringWidth("Direction " + EnumFormatter.getFormattedText(PlayerUtil.getDirection()) + " [" + PlayerUtil.getAxis(PlayerUtil.getDirection()) + "]");
    }

    @Override
    public float getHeight() {
        return getFontHeight();
    }
}
