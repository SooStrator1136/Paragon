package com.paragon.client.systems.module.impl.movement;

import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;

public class Speed extends Module {

    private ModeSetting<Mode> mode = new ModeSetting<>("Mode", "The mode to use", Mode.CPVP_CC);

    // CPVP.cc settings
    private NumberSetting friction = (NumberSetting) new NumberSetting("Friction", "Friction to apply", 0.8f, 0f, 1f, 0.1f).setVisiblity(() -> mode.getCurrentMode() == Mode.CPVP_CC);

    // Normal mode
    private NumberSetting speed = (NumberSetting) new NumberSetting("Speed", "The speed multiplier", 0.6f, 0.1f, 1.5f, 0.1f).setVisiblity(() -> mode.getCurrentMode() == Mode.VANILLA_STRAFE);

    private BooleanSetting disableMotion = new BooleanSetting("Disable Motion", "Cancels your motion when you disable", false);

    public Speed() {
        super("Speed", ModuleCategory.MOVEMENT, "Increase the player's speed");
        this.addSettings(mode, friction, speed, disableMotion);
    }

    @Override
    public void onDisable() {
        mc.player.speedInAir = 0.02F;

        if (disableMotion.isEnabled()) {
            mc.player.setVelocity(0, 0, 0);
        }
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        switch (mode.getCurrentMode()) {
            case CPVP_CC:
                if (mc.player.onGround) {
                    mc.player.jump();
                    mc.player.speedInAir =  0.02f;
                    mc.player.motionX *= friction.getValue();
                    mc.player.motionZ *= friction.getValue();
                } else {
                    mc.player.speedInAir = 0.0226f;
                }
                break;
            case VANILLA_STRAFE:
                if (mc.player.onGround) {
                    mc.player.jump();
                }

                mc.player.speedInAir = speed.getValue() / 10;
                break;
        }
    }

    public enum Mode {
        /**
         * A speed mode intended for use on cpvp.cc
         */
        CPVP_CC,

        /**
         * A simple vanilla strafe
         */
        VANILLA_STRAFE
    }

}
