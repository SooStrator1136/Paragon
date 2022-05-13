package com.paragon.client.systems.module.impl.movement;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.util.player.InventoryUtil;
import com.paragon.asm.mixins.accessor.ICPacketPlayer;
import com.paragon.asm.mixins.accessor.IPlayerControllerMP;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.GameType;

/**
 * @author Wolfsurge
 */
public class NoFall extends Module {

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.VANILLA)
            .setDescription("How to prevent fall damage");

    private final Setting<Boolean> spoofFall = new Setting<>("Spoof Fall", false)
            .setDescription("Spoof fall distance")
            .setVisibility(() -> mode.getValue().equals(Mode.RUBBERBAND));

    private final Setting<Boolean> ignoreElytra = new Setting<>("Ignore Elytra", true)
            .setDescription("Don't attempt to place a water bucket when flying with an elytra")
            .setVisibility(() -> mode.getValue().equals(Mode.BUCKET));

    public NoFall() {
        super("NoFall", Category.MOVEMENT, "Disables fall damage");
        this.addSettings(mode, spoofFall);
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        // Ignore if we are flying with an elytra, or we are in creative mode
        if (mc.player.isElytraFlying() && ignoreElytra.getValue() || mc.playerController.getCurrentGameType().equals(GameType.CREATIVE)) {
            return;
        }

        // We are going to take damage from falling, and we aren't over water
        if (mc.player.fallDistance > 3 && !mc.player.isOverWater()) {
            switch (mode.getValue()) {
                case VANILLA:
                    // Send a packet that says that we are on the ground
                    mc.player.connection.sendPacket(new CPacketPlayer(true));
                    break;

                case RUBBERBAND:
                    // Send an invalid packet
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.motionX, 0, mc.player.motionZ, true));

                    // Set the fall distance to 0
                    if (spoofFall.getValue()) {
                        mc.player.fallDistance = 0;
                    }

                    break;

                case BUCKET:
                    // Don't do anything if we don't have a water bucket
                    if (InventoryUtil.getItemInHotbar(Items.WATER_BUCKET) != -1) {
                        // Switch to water bucket
                        InventoryUtil.switchToSlot(InventoryUtil.getItemInHotbar(Items.WATER_BUCKET), false);

                        // Sync
                        ((IPlayerControllerMP) mc.playerController).hookSyncCurrentPlayItem();

                        // Send rotation packet
                        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, 90, false));

                        // Set client rotation
                        mc.player.rotationPitch = 90;

                        // Attempt to place water bucket
                        mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                    }
            }
        }
    }

    @Listener
    public void onPacketSent(PacketEvent.PreSend event) {
        // Ignore if we are flying with an elytra, or we are in creative mode
        if (mc.player.isElytraFlying() && ignoreElytra.getValue() || mc.playerController.getCurrentGameType().equals(GameType.CREATIVE)) {
            return;
        }

        // Packet is a CPacketPlayer
        if (event.getPacket() instanceof CPacketPlayer) {
            if (mode.getValue().equals(Mode.PACKET_MODIFY)) {
                // Set packet Y
                ((ICPacketPlayer) event.getPacket()).setY(mc.player.posY + 1);

                // Set packet onGround
                ((ICPacketPlayer) event.getPacket()).setOnGround(true);
            }
        }
    }

    public enum Mode {
        /**
         * One simple packet to negate fall damage
         */
        VANILLA,

        /**
         * Invalid packet to lag us back
         */
        RUBBERBAND,

        /**
         * Modify packet about to be sent to the server
         */
        PACKET_MODIFY,

        /**
         * Attempt to place water bucket beneath us
         */
        BUCKET
    }

}
