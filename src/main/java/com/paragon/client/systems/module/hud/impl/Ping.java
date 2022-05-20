package com.paragon.client.systems.module.hud.impl;

import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.impl.client.Colours;
import net.minecraft.util.text.TextFormatting;

public class Ping extends HUDModule {

    public static Ping INSTANCE;

    public Ping() {
        super("Ping", "Displays your ping in ms");

        INSTANCE = this;
    }

    @Override
    public void render() {
        renderText("Ping: " + TextFormatting.WHITE + getPing() + "ms", getX(), getY(), Colours.mainColour.getValue().getRGB());
    }

    @Override
    public float getWidth() {
        return getStringWidth("Ping: " + getPing() + "ms");
    }

    @Override
    public float getHeight() {
        return getFontHeight();
    }

    public int getPing() {
        if (mc.getConnection() != null && mc.getConnection().getPlayerInfo(mc.getSession().getProfile().getId()) != null) {
            return mc.getConnection().getPlayerInfo(mc.getSession().getProfile().getId()).getResponseTime();
        }

        return -1;
    }
}
