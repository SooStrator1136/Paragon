package com.paragon.client.systems.module.impl.movement;

import com.paragon.api.event.player.TravelEvent;
import com.paragon.api.util.player.PlayerUtil;
import com.paragon.api.util.string.EnumFormatter;
import com.paragon.asm.mixins.accessor.ITimer;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Wolfsurge
 */
public class ElytraFlight extends Module {

    // Mode for elytra flight
    private final ModeSetting<Mode> mode = new ModeSetting<>("Mode", "The mode to use", Mode.CONTROL);

    // Strict settings
    private final NumberSetting ascendPitch = (NumberSetting) new NumberSetting("Ascend Pitch", "What value to set your pitch to when ascending", -45, -90, 90, 1).setParentSetting(mode).setVisiblity(() -> mode.getCurrentMode() == Mode.STRICT);
    private final NumberSetting descendPitch = (NumberSetting) new NumberSetting("Descend Pitch", "What value to set your pitch to when descending", 45, -90, 90, 1).setParentSetting(mode).setVisiblity(() -> mode.getCurrentMode() == Mode.STRICT);
    private final BooleanSetting lockPitch = (BooleanSetting) new BooleanSetting("Lock Pitch", "Lock your pitch when you are not ascending or descending", true).setParentSetting(mode).setVisiblity(() -> mode.getCurrentMode() == Mode.STRICT);
    private final NumberSetting lockPitchVal = (NumberSetting) new NumberSetting("Locked Pitch", "The pitch to lock you to when you are not ascending or descending", 0, -90, 90, 1).setParentSetting(mode).setVisiblity(() -> mode.getCurrentMode() == Mode.STRICT);

    // Boost settings
    private final BooleanSetting cancelMotion = (BooleanSetting) new BooleanSetting("Cancel Motion", "Stop motion when not moving", false).setParentSetting(mode).setVisiblity(() -> mode.getCurrentMode() == Mode.BOOST);

    // Global settings
    private final NumberSetting flySpeed = new NumberSetting("Fly Speed", "How fast to fly", 1, 0.1f, 2f, 0.1f);
    private final NumberSetting ascend = (NumberSetting) new NumberSetting("Ascend Speed", "How fast to ascend", 1, 0.1f, 2f, 0.1f).setVisiblity(() -> mode.getCurrentMode() == Mode.CONTROL || mode.getCurrentMode() == Mode.STRICT);
    private final NumberSetting descend = (NumberSetting) new NumberSetting("Descend Speed", "How fast to descend", 1, 0.1f, 2f, 0.1f).setVisiblity(() -> mode.getCurrentMode() == Mode.CONTROL || mode.getCurrentMode() == Mode.STRICT);
    private final NumberSetting fallSpeed = new NumberSetting("Fall Speed", "How fast to fall", 0, 0, 0.1f, 0.01f);

    // Takeoff settings
    private final BooleanSetting takeOff = new BooleanSetting("Takeoff", "Automatically take off when you enable the module", true);
    private final NumberSetting takeOffTimer = (NumberSetting) new NumberSetting("Timer", "How long a tick lasts for", 0.2f, 0.1f, 1, 0.1f).setParentSetting(takeOff);

    public ElytraFlight() {
        super("ElytraFlight", ModuleCategory.MOVEMENT, "Allows for easier flight with an elytra");
        this.addSettings(mode, flySpeed, ascend, descend, fallSpeed, takeOff);
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            return;
        }

        if (takeOff.isEnabled()) {
            // Make sure we aren't elytra flying
            if (!mc.player.isElytraFlying()) {
                // Make the game slower
                ((ITimer) mc.timer).setTickLength(50 / takeOffTimer.getValue());

                if (mc.player.onGround) {
                    // Jump if we're on the ground
                    mc.player.jump();
                } else {
                    // Make us fly if we are off the ground
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                }
            }
        }
    }

    @Override
    public void onDisable() {
        // Set us back to normal speed
        ((ITimer) mc.timer).setTickLength(50);
    }

    @Listener
    public void onTravel(TravelEvent travelEvent) {
        if (nullCheck()) {
            return;
        }

        if (mc.player.isElytraFlying()) {
            // Set us to normal speed if we are flying
            ((ITimer) mc.timer).setTickLength(50);

            if (mode.getCurrentMode() != Mode.BOOST) {
                // Cancel motion
                travelEvent.cancel();

                // Make us fall
                PlayerUtil.stopMotion(-fallSpeed.getValue());
            } else {
                if (cancelMotion.isEnabled()) {
                    // Cancel motion
                    travelEvent.cancel();

                    // Make us fall
                    PlayerUtil.stopMotion(-fallSpeed.getValue());
                }
            }

            switch (mode.getCurrentMode()) {
                case CONTROL:
                    // Move
                    PlayerUtil.move(flySpeed.getValue());

                    // Handle moving up and down
                    handleControl();
                    break;
                case STRICT:
                    // Move
                    PlayerUtil.move(flySpeed.getValue());

                    // Handle moving up and down
                    handleStrict();
                    break;
                case BOOST:
                    if (mc.gameSettings.keyBindForward.isKeyDown()) {
                        // Move forward
                        PlayerUtil.propel(flySpeed.getValue() * (cancelMotion.isEnabled() ? 1 : 0.015f));
                    }
                    break;
            }

            // Lock our limbs
            PlayerUtil.lockLimbs();
        }
    }

    public void handleControl() {
        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            // Increase Y
            mc.player.motionY = ascend.getValue();
        }

        if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            // Decrease Y
            mc.player.motionY = -descend.getValue();
        }
    }

    public void handleStrict() {
        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            // Increase pitch
            mc.player.rotationPitch = ascendPitch.getValue();

            // Increase Y
            mc.player.motionY = ascend.getValue();
        } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            // Decrease pitch
            mc.player.rotationPitch = descendPitch.getValue();

            // Decrease Y
            mc.player.motionY = -descend.getValue();
        } else {
            if (lockPitch.isEnabled()) {
                // Set pitch if we aren't moving
                mc.player.rotationPitch = lockPitchVal.getValue();
            }
        }
    }

    @Override
    public String getModuleInfo() {
        return " " + EnumFormatter.getFormattedText(mode.getCurrentMode());
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
