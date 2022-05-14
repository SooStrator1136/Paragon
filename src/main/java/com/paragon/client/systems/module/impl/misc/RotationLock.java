package com.paragon.client.systems.module.impl.misc;

import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.setting.Setting;

/**
 * @author Wolfsurge
 */
public class RotationLock extends Module {

    private final Setting<Float> yaw = new Setting<>("Yaw", 0f, -180f, 180f, 1f)
            .setDescription("The yaw to lock to");

    private final Setting<Float> pitch = new Setting<>("Pitch", 0f, -180f, 180f, 1f)
            .setDescription("The pitch to lock to");

    public RotationLock() {
        super("RotationLock", Category.MISC, "Locks your rotation");
        this.addSettings(yaw, pitch);
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
