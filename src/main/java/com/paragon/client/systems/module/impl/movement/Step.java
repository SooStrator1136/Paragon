package com.paragon.client.systems.module.impl.movement;

import com.paragon.Paragon;
import com.paragon.api.event.player.StepEvent;
import com.paragon.api.util.player.PlayerUtil;
import com.paragon.api.util.string.StringUtil;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import com.paragon.api.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * @author Wolfsurge, Doogie13
 * Used Cosmos' StepEvent (Doogie's original code was buggy)
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
        if (nullCheck() ||
                // wtf
                !isEnabled()) {
            return;
        }

        // Set step height
        mc.player.stepHeight = mode.getValue().equals(Mode.VANILLA) ? stepHeight.getValue() : 1f;
    }

    @Listener
    public void onStep(StepEvent event) {
        if (mode.getValue().equals(Mode.PACKET) && event.getEntity().equals(mc.player)) {
            double height = event.getBB().minY - mc.player.posY;

            double[] forward = PlayerUtil.forward(0.1);

            // Don't step
            if (height <= 0.0 || height > 1 || mc.player.lastTickPosY != mc.player.posY || mc.player.isInWater() || mc.player.isInLava() || mc.gameSettings.keyBindJump.isKeyDown() || mc.player.fallDistance != 0 || !mc.player.onGround || mc.world.collidesWithAnyBlock(mc.player.getEntityBoundingBox().offset(forward[0], 0.9, forward[2]))) {
                return;
            }

            // Step offsets
            double[] offsets;

            // Chests
            if (height == 0.875) {
                offsets = new double[]{ 0.42, 0.75, 0.43 };
            }

            // Normal blocks
            else if (height == 1) {
                offsets = new double[]{ 0.42, 0.75 };
            }

            // IDK
            else {
                return;
            }

            // Send offsets
            for (double offset : offsets) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + offset, mc.player.posZ, false));
            }
        }
    }

    @Override
    public String getData() {
        return " " + StringUtil.getFormattedText(mode.getValue());
    }

    public enum Mode {
        /**
         * Vanilla step - bypasses almost no servers :P
         */
        VANILLA,

        /**
         * Packet step - Higher chance of bypassing
         */
        PACKET
    }

}
