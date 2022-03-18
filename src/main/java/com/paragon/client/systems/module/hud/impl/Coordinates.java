package com.paragon.client.systems.module.hud.impl;

import com.paragon.client.systems.module.hud.HUDModule;

public class Coordinates extends HUDModule {

    public Coordinates() {
        super("Coordinates", "Displays your coordinates");
    }

    @Override
    public void render() {
        renderText(mc.player.posX + " " + mc.player.posY + " " + mc.player.posZ, getX(), getY(), -1);
    }
}
