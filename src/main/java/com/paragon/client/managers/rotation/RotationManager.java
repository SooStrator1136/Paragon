package com.paragon.client.managers.rotation;

import com.paragon.Paragon;
import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.event.player.RotationUpdateEvent;
import com.paragon.api.event.player.UpdateEvent;
import com.paragon.api.util.Wrapper;
import com.paragon.asm.mixins.accessor.ICPacketPlayer;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;
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

    @Listener
    public void onRotationUpdate(RotationUpdateEvent event) {
        if (!rotationsQueue.isEmpty()) {
            event.cancel();
        }
    }

    @Listener
    public void onPacketSend(PacketEvent.PreSend event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            if (!rotationsQueue.isEmpty()) {
                rotationsQueue.sort(Comparator.comparing(rotation -> rotation.getPriority().getPriority()));

                Rotation rotation = rotationsQueue.get(0);

                ((ICPacketPlayer) event.getPacket()).setYaw(rotation.getYaw());
                ((ICPacketPlayer) event.getPacket()).setPitch(rotation.getPitch());

                mc.player.rotationYaw = rotation.getYaw();
                mc.player.rotationPitch = rotation.getPitch();

                // Remove rotations that have the same yaw as the one we just rotated to
                rotationsQueue.removeIf(rotation1 -> rotation1.getYaw() == rotation.getYaw());
            }
        }
    }

    @Listener
    public void onUpdate(UpdateEvent event) {
        if (!rotationsQueue.isEmpty()) {
            // Send packet if we haven't cleared the queue yet
            mc.player.connection.sendPacket(new CPacketPlayer());
        }
    }

    public void addRotation(Rotation rotation) {
        this.rotationsQueue.add(rotation);
    }

}
