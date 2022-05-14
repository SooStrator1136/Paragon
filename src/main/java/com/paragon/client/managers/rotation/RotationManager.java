package com.paragon.client.managers.rotation;

import com.paragon.Paragon;
import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.util.Wrapper;
import com.paragon.asm.mixins.accessor.ICPacketPlayer;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Comparator;

public class RotationManager implements Wrapper {

    private final ArrayList<Rotation> rotationsQueue = new ArrayList<>();

    public RotationManager() {
        Paragon.INSTANCE.getEventBus().register(this);
    }

    @Listener
    public void onPacketSent(PacketEvent.PreSend preSend) {
        if (nullCheck()) {
            return;
        }

        if (preSend.getPacket() instanceof CPacketPlayer.Rotation) {
            if (!rotationsQueue.isEmpty()) {
                preSend.cancel();

                rotationsQueue.sort(Comparator.comparing(rotation -> rotation.getPriority().getPriority()));

                CPacketPlayer packet = (CPacketPlayer.Rotation) preSend.getPacket();

                ((ICPacketPlayer) preSend.getPacket()).setYaw(rotationsQueue.get(0).getYaw());
                ((ICPacketPlayer) preSend.getPacket()).setPitch(rotationsQueue.get(0).getPitch());

                if (rotationsQueue.get(0).getRotate().equals(Rotate.LEGIT)) {
                    mc.player.rotationYaw = rotationsQueue.get(0).getYaw();
                    mc.player.rotationYawHead = rotationsQueue.get(0).getYaw();
                    mc.player.rotationPitch = rotationsQueue.get(0).getPitch();
                }

                rotationsQueue.remove(0);

                mc.player.connection.sendPacket(packet);
            }
        }
    }

    public void addRotation(Rotation rotation) {
        this.rotationsQueue.add(rotation);
    }

}
