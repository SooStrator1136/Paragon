package com.paragon.client.systems.module.impl.movement;

import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.NumberSetting;

public class Flight extends Module {

    private final NumberSetting flySpeed = new NumberSetting("Fly Speed", "How fast you fly", 0.05f, 0.01f, 0.1f, 0.01f);

    public Flight() {
        super("Flight", ModuleCategory.MOVEMENT, "Allows you to fly in survival mode");
        this.addSettings(flySpeed);
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
