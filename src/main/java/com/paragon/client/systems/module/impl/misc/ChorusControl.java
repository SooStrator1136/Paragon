package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.module.Category;
import com.paragon.api.module.Module;
import com.paragon.api.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;


import java.awt.*;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class ChorusControl extends Module {

    public Setting<Boolean> cPacketPlayer = new Setting<>("cPacketPlayer", true);
    public Setting<Boolean> packetPlayerPosLook = new Setting<>("SPacketPlayerPosLook", true);

    Queue<CPacketPlayer> packets = new LinkedList<>();
    Queue<CPacketConfirmTeleport> teleportPackets = new LinkedList<>();


    SPacketPlayerPosLook sPacketPlayerPosLook;


    public ChorusControl() {

        super("ChorusControl", Category.MISC, "Cancels packets to let you not teleport");
    }

    @Listener
    public void onPacketReceive(PacketEvent.PreReceive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            sPacketPlayerPosLook = (SPacketPlayerPosLook) event.getPacket();
            if (packetPlayerPosLook.getValue()) event.cancel();
        }

        if (event.getPacket() instanceof CPacketPlayer) {
            packets.add(((CPacketPlayer) event.getPacket()));

            if (cPacketPlayer.getValue())
                event.cancel();
        }

        if (event.getPacket() instanceof CPacketConfirmTeleport) {
            teleportPackets.add(((CPacketConfirmTeleport) event.getPacket()));
            event.cancel();
        }
    }

    @Override
    public void onDisable() {
        while (!this.packets.isEmpty()) {
            mc.getConnection().sendPacket(Objects.requireNonNull(this.packets.poll()));
        }
        while (!this.teleportPackets.isEmpty()) {
            mc.getConnection().sendPacket(Objects.requireNonNull(this.teleportPackets.poll()));
        }
        sPacketPlayerPosLook = null;
    }
}
