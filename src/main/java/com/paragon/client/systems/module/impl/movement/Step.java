package com.paragon.client.systems.module.impl.movement;

import com.paragon.api.util.player.PlayerUtil;
import com.paragon.api.util.string.EnumFormatter;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * @author Wolfsurge
 */
public class Step extends Module {

    public static Step INSTANCE;

    // Step mode
    public static Setting<Mode> mode = new Setting<>("Mode", Mode.PACKET)
            .setDescription("What mode to use");

    // Vanilla step height
    public static Setting<Float> stepHeight = new Setting<>("StepHeight", 1.5f, 0.5f, 2.5f, 0.5f)
            .setDescription("How high to step up")
            .setVisibility(() -> mode.getValue().equals(Mode.VANILLA));

    public Step() {
        super("Step", Category.MOVEMENT, "Lets you instantly step up blocks");

        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        if (nullCheck()) {
            return;
        }

        // Set step height to normal
        mc.player.stepHeight = 0.5f;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (nullCheck()) {
            return;
        }

        switch (mode.getValue()) {
            case PACKET:
                // Set our position if we are: collided, on ground, not falling, not on a ladder, and not jumping
                if (mc.player.collidedHorizontally && mc.player.onGround && mc.player.fallDistance == 0.0f && !mc.player.isOnLadder() && !mc.player.movementInput.jump) {
                    // Send packet
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.42, mc.player.posZ, true));

                    // Set position
                    mc.player.setPosition(mc.player.posX, mc.player.posY + 0.75, mc.player.posZ);

                    // We want to move a tiny bit forwards
                    PlayerUtil.move(0.01f);
                }
                break;

            case VANILLA:
                // Increase step height
                mc.player.stepHeight = stepHeight.getValue();
                break;

        }

    }

    @Override
    public String getArrayListInfo() {
        return " " + EnumFormatter.getFormattedText(mode.getValue());
    }

    public enum Mode {
        /**
         * Vanilla step - bypasses almost no servers :P
         */
        VANILLA,

        /**
         * Packet step - Higher chance of bypassing, probably
         */
        PACKET
    }

}
