package com.paragon.client.systems.module.impl.movement;

import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import com.paragon.api.setting.Setting;

public class Flight extends Module {

    public static Flight INSTANCE;

    public static Setting<Float> flySpeed = new Setting<>("FlySpeed", 0.05f, 0.01f, 0.1f, 0.01f)
            .setDescription("How fast you fly");

    public Flight() {
        super("Flight", Category.MOVEMENT, "Allows you to fly in survival mode");

        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        mc.player.capabilities.setFlySpeed(0.05f);
        mc.player.capabilities.isFlying = false;
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        mc.player.capabilities.setFlySpeed(flySpeed.getValue());
        mc.player.capabilities.isFlying = true;
    }

}
