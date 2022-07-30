package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.module.Category;
import com.paragon.api.module.Module;
import com.paragon.api.setting.Setting;

/**
 * @author Surge
 */
public class RotationLock extends Module {

    public static RotationLock INSTANCE;

    public static Setting<Float> yaw = new Setting<>("Yaw", 0f, -180f, 180f, 1f)
            .setDescription("The yaw to lock to");

    public static Setting<Float> pitch = new Setting<>("Pitch", 0f, -180f, 180f, 1f)
            .setDescription("The pitch to lock to");

    public RotationLock() {
        super("RotationLock", Category.MISC, "Locks your rotation");

        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        mc.player.rotationYaw = yaw.getValue();
        mc.player.rotationYawHead = yaw.getValue();
        mc.player.rotationPitch = pitch.getValue();
    }
}
