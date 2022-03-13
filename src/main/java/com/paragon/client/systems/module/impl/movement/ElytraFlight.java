package com.paragon.client.systems.module.impl.movement;

import com.paragon.api.event.player.TravelEvent;
import com.paragon.api.util.player.PlayerUtil;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ElytraFlight extends Module {

    private ModeSetting<Mode> mode = new ModeSetting<>("Mode", "The mode to use", Mode.CONTROL);

    // Strict settings
    private NumberSetting ascendPitch = (NumberSetting) new NumberSetting("Ascend Pitch", "What value to set your pitch to when ascending", -45, -90, 90, 1).setParentSetting(mode).setVisiblity(() -> mode.getCurrentMode() == Mode.STRICT);
    private NumberSetting descendPitch = (NumberSetting) new NumberSetting("Descend Pitch", "What value to set your pitch to when descending", 45, -90, 90, 1).setParentSetting(mode).setVisiblity(() -> mode.getCurrentMode() == Mode.STRICT);
    private BooleanSetting lockPitch = (BooleanSetting) new BooleanSetting("Lock Pitch", "Lock your pitch when you are not ascending or descending", true).setParentSetting(mode).setVisiblity(() -> mode.getCurrentMode() == Mode.STRICT);
    private NumberSetting lockPitchVal = (NumberSetting) new NumberSetting("Locked Pitch", "The pitch to lock you to when you are not ascending or descending", 0, -90, 90, 1).setParentSetting(mode).setVisiblity(() -> mode.getCurrentMode() == Mode.STRICT);

    // Boost settings
    private BooleanSetting cancelMotion = (BooleanSetting) new BooleanSetting("Cancel Motion", "Stop motion when not moving", false).setParentSetting(mode).setVisiblity(() -> mode.getCurrentMode() == Mode.BOOST);

    private NumberSetting flySpeed = new NumberSetting("Fly Speed", "How fast to fly", 1, 0.1f, 2f, 0.1f);
    private NumberSetting ascend = (NumberSetting) new NumberSetting("Ascend Speed", "How fast to ascend", 1, 0.1f, 2f, 0.1f).setVisiblity(() -> mode.getCurrentMode() == Mode.CONTROL || mode.getCurrentMode() == Mode.STRICT);
    private NumberSetting descend = (NumberSetting) new NumberSetting("Descend Speed", "How fast to descend", 1, 0.1f, 2f, 0.1f).setVisiblity(() -> mode.getCurrentMode() == Mode.CONTROL || mode.getCurrentMode() == Mode.STRICT);
    private NumberSetting fallSpeed = new NumberSetting("Fall Speed", "How fast to fall", 0, 0, 0.1f, 0.01f);

    public ElytraFlight() {
        super("ElytraFlight", ModuleCategory.MOVEMENT, "Allows for easier flight with an elytra");
        this.addSettings(mode, flySpeed, ascend, descend, fallSpeed);
    }

    @Listener
    public void onTravel(TravelEvent travelEvent) {
        if (nullCheck()) {
            return;
        }

        if (mc.player.isElytraFlying()) {
            if (mode.getCurrentMode() != Mode.BOOST) {
                travelEvent.cancel();
                PlayerUtil.stopMotion(-fallSpeed.getValue());
            } else {
                if (cancelMotion.isEnabled()) {
                    travelEvent.cancel();
                    PlayerUtil.stopMotion(-fallSpeed.getValue());
                }
            }

            switch (mode.getCurrentMode()) {
                case CONTROL:
                    PlayerUtil.move(flySpeed.getValue());
                    handleControl();
                    break;
                case STRICT:
                    PlayerUtil.move(flySpeed.getValue());
                    handleStrict();
                    break;
                case BOOST:
                    if (mc.gameSettings.keyBindForward.isKeyDown()) {
                        PlayerUtil.propel(flySpeed.getValue() * (cancelMotion.isEnabled() ? 1 : 0.015f));
                    }
                    break;
            }

            PlayerUtil.lockLimbs();
        }
    }

    public void handleControl() {
        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.motionY = ascend.getValue();
        }

        if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            mc.player.motionY = -descend.getValue();
        }
    }

    public void handleStrict() {
        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.rotationPitch = ascendPitch.getValue();
            mc.player.motionY = ascend.getValue();
        } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            mc.player.rotationPitch = descendPitch.getValue();
            mc.player.motionY = -descend.getValue();
        } else {
            if (lockPitch.isEnabled()) {
                mc.player.rotationPitch = lockPitchVal.getValue();
            }
        }
    }

    public enum Mode {
        /**
         * Lets you fly without idle gliding
         */
        CONTROL,

        /**
         * Lets you fly on strict servers
         */
        STRICT,

        /**
         * Boost yourself when using an elytra
         */
        BOOST
    }

}
