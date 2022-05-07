package com.paragon.client.systems.module.impl.movement;

import com.paragon.api.event.player.TravelEvent;
import com.paragon.api.util.player.PlayerUtil;
import com.paragon.api.util.string.EnumFormatter;
import com.paragon.asm.mixins.accessor.IMinecraft;
import com.paragon.asm.mixins.accessor.ITimer;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.network.play.client.CPacketEntityAction;

/**
 * @author Wolfsurge
 */
public class ElytraFlight extends Module {

    // Mode for elytra flight
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.CONTROL)
            .setDescription("The mode to use");

    // Strict settings
    private final Setting<Float> ascendPitch = new Setting<>("Ascend Pitch", -45f, -90f, 90f, 1f)
            .setDescription("What value to set your pitch to when ascending")
            .setParentSetting(mode)
            .setVisibility(() -> mode.getValue().equals(Mode.STRICT));

    private final Setting<Float> descendPitch = new Setting<>("Descend Pitch", 45f, -90f, 90f, 1f)
            .setDescription("What value to set your pitch to when descending")
            .setParentSetting(mode)
            .setVisibility(() -> mode.getValue().equals(Mode.STRICT));

    private final Setting<Boolean> lockPitch = new Setting<>("Lock Pitch", true)
            .setDescription("Lock your pitch when you are not ascending or descending")
            .setParentSetting(mode)
            .setVisibility(() -> mode.getValue().equals(Mode.STRICT));

    private final Setting<Float> lockPitchVal = new Setting<>("Locked Pitch", 0f, -90f, 90f, 1f)
            .setDescription("The pitch to lock you to when you are not ascending or descending")
            .setParentSetting(mode)
            .setVisibility(() -> mode.getValue().equals(Mode.STRICT));

    // Boost settings
    private final Setting<Boolean> cancelMotion = new Setting<>("Cancel Motion", false)
            .setDescription("Stop motion when not moving")
            .setParentSetting(mode)
            .setVisibility(() -> mode.getValue().equals(Mode.BOOST));

    // Global settings
    private final Setting<Float> flySpeed = new Setting<>("Fly Speed", 1f, 0.1f, 2f, 0.1f)
            .setDescription("The speed to fly at");

    private final Setting<Float> ascend = new Setting<>("Ascend Speed", 1f, 0.1f, 2f, 0.1f)
            .setDescription("How fast to ascend")
            .setVisibility(() -> !mode.getValue().equals(Mode.BOOST));

    private final Setting<Float> descend = new Setting<>("Descend Speed", 1f, 0.1f, 2f, 0.1f)
            .setDescription("How fast to descend")
            .setVisibility(() -> !mode.getValue().equals(Mode.BOOST));

    private final Setting<Float> fallSpeed = new Setting<>("Fall Speed", 0f, 0f, 0.1f, 0.01f)
            .setDescription("How fast to fall");

    // Takeoff settings
    private final Setting<Boolean> takeOff = new Setting<>("Takeoff", true)
            .setDescription("Automatically take off when you enable the module");

    private final Setting<Float> takeOffTimer = new Setting<>("Timer", 0.2f, 0.1f, 1f, 0.1f)
            .setDescription("How long a tick lasts for")
            .setParentSetting(takeOff);

    public ElytraFlight() {
        super("ElytraFlight", ModuleCategory.MOVEMENT, "Allows for easier flight with an elytra");
        this.addSettings(mode, flySpeed, ascend, descend, fallSpeed, takeOff);
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            return;
        }

        if (takeOff.getValue()) {
            // Make sure we aren't elytra flying
            if (!mc.player.isElytraFlying()) {
                // Make the game slower
                ((ITimer) ((IMinecraft) mc).getTimer()).setTickLength(50 / takeOffTimer.getValue());

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
        ((ITimer) ((IMinecraft) mc).getTimer()).setTickLength(50);
    }

    @Listener
    public void onTravel(TravelEvent travelEvent) {
        if (nullCheck()) {
            return;
        }

        if (mc.player.isElytraFlying()) {
            // Set us to normal speed if we are flying
            ((ITimer) ((IMinecraft) mc).getTimer()).setTickLength(50);

            if (mode.getValue() != Mode.BOOST) {
                // Cancel motion
                travelEvent.cancel();

                // Make us fall
                PlayerUtil.stopMotion(-fallSpeed.getValue());
            } else {
                if (cancelMotion.getValue()) {
                    // Cancel motion
                    travelEvent.cancel();

                    // Make us fall
                    PlayerUtil.stopMotion(-fallSpeed.getValue());
                }
            }

            switch (mode.getValue()) {
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
                        PlayerUtil.propel(flySpeed.getValue() * (cancelMotion.getValue() ? 1 : 0.015f));
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
            if (lockPitch.getValue()) {
                // Set pitch if we aren't moving
                mc.player.rotationPitch = lockPitchVal.getValue();
            }
        }
    }

    @Override
    public String getArrayListInfo() {
        return " " + EnumFormatter.getFormattedText(mode.getValue());
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
