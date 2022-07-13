package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.util.calculations.Timer;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import com.paragon.api.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Wolfsurge
 */
@SideOnly(Side.CLIENT)
public class Blink extends Module {

    public static Blink INSTANCE;

    // General
    public static Setting<Mode> mode = new Setting<>("Mode", Mode.PACKETS_QUEUED)
            .setDescription("When to send queued packets");

    // Packet queue flush settings
    public static Setting<Double> queueLength = new Setting<>("QueueLength", 50D, 1D, 1000D, 1D)
            .setDescription("The size of the queue to start sending packets")
            .setParentSetting(mode)
            .setVisibility(() -> mode.getValue().equals(Mode.PACKETS_QUEUED));

    public static Setting<Double> delay = new Setting<>("Delay", 4D, 0.1D, 10D, 0.1D)
            .setDescription("The delay between sending packets in seconds")
            .setParentSetting(mode)
            .setVisibility(() -> mode.getValue().equals(Mode.DELAY));

    public static Setting<Double> distance = new Setting<>("Distance", 10D, 1D, 100D, 0.1D)
            .setDescription("The distance to the fake player to start sending packets")
            .setParentSetting(mode)
            .setVisibility(() -> mode.getValue().equals(Mode.DISTANCE));

    // Using CopyOnWriteArrayList to avoid ConcurrentModificationException
    private final List<CPacketPlayer> packetQueue = new CopyOnWriteArrayList<>();
    private final Timer timer = new Timer();
    private BlockPos lastPosition;

    public Blink() {
        super("Blink", Category.MISC, "Cancels sending packets for a length of time");

        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            return;
        }

        EntityOtherPlayerMP fakePlayer = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());

        fakePlayer.copyLocationAndAnglesFrom(mc.player);
        fakePlayer.rotationYawHead = mc.player.rotationYawHead;
        fakePlayer.inventory.copyInventory(mc.player.inventory);
        fakePlayer.setSneaking(mc.player.isSneaking());
        fakePlayer.setPrimaryHand(mc.player.getPrimaryHand());

        mc.world.addEntityToWorld(-351352, fakePlayer);

        lastPosition = mc.player.getPosition();
    }

    @Override
    public void onDisable() {
        if (nullCheck()) {
            return;
        }

        sendPackets();

        mc.world.removeEntityFromWorld(-351352);
        lastPosition = null;
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        if (lastPosition == null) {
            lastPosition = mc.player.getPosition();
        }

        switch (mode.getValue()) {
            case PACKETS_QUEUED:
                if (packetQueue.size() >= queueLength.getValue()) {
                    sendPackets();
                }

                break;

            case DELAY:
                if (timer.hasMSPassed(delay.getValue() * 1000)) {
                    sendPackets();
                    timer.reset();
                }

                break;

            case DISTANCE:
                if (mc.player.getDistance(lastPosition.getX(), lastPosition.getY(), lastPosition.getZ()) >= distance.getValue()) {
                    sendPackets();
                }

                break;
        }
    }

    @Listener
    public void onPacketSent(PacketEvent.PreSend event) {
        if (nullCheck()) {
            return;
        }

        if (event.getPacket() instanceof CPacketPlayer) {
            event.cancel();
            packetQueue.add((CPacketPlayer) event.getPacket());
        }
    }

    public void sendPackets() {
        lastPosition = mc.player.getPosition();

        mc.world.removeEntityFromWorld(-351352);

        if (!packetQueue.isEmpty()) {
            packetQueue.forEach(packet -> mc.player.connection.sendPacket(packet));
            packetQueue.clear();
        }

        EntityOtherPlayerMP fakePlayer = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());

        fakePlayer.copyLocationAndAnglesFrom(mc.player);
        fakePlayer.rotationYawHead = mc.player.rotationYawHead;
        fakePlayer.inventory.copyInventory(mc.player.inventory);
        fakePlayer.setSneaking(mc.player.isSneaking());
        fakePlayer.setPrimaryHand(mc.player.getPrimaryHand());

        mc.world.addEntityToWorld(-351352, fakePlayer);
    }

    public enum Mode {
        /**
         * Send queued packets after a certain amount of packets have been queued
         */
        PACKETS_QUEUED,

        /**
         * Send queued packets after you have reached a distance away from the fake player
         */
        DISTANCE,

        /**
         * Send queued packets after you have reached a certain amount of time
         */
        DELAY,

        /**
         * Manually send queued packets by toggling the module
         */
        MANUAL
    }

}