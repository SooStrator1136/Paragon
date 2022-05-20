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
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

public class RotationManager implements Wrapper {

    private final CopyOnWriteArrayList<Rotation> rotationsQueue = new CopyOnWriteArrayList<>();

    public RotationManager() {
        MinecraftForge.EVENT_BUS.register(this);
        Paragon.INSTANCE.getEventBus().register(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (nullCheck()) {
            rotationsQueue.clear();
            return;
        }

        rotationsQueue.sort(Comparator.comparing(rotation -> rotation.getPriority().getPriority()));

        for (Rotation rotation : rotationsQueue) {
            switch (rotation.getRotate()) {
                case LEGIT:
                    mc.player.rotationYaw = rotation.getYaw();
                    mc.player.rotationYawHead = rotation.getYaw();
                    mc.player.rotationPitch = rotation.getPitch();
                    break;

                case PACKET:
                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotation.getYaw(), rotation.getPitch(), mc.player.onGround));
                    break;
            }
        }

        rotationsQueue.clear();
    }

    public void addRotation(Rotation rotation) {
        this.rotationsQueue.add(rotation);
    }

}
