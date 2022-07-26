package com.paragon.client.managers;

import com.paragon.Paragon;
import com.paragon.api.event.combat.PlayerDeathEvent;
import com.paragon.api.event.combat.TotemPopEvent;
import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.event.world.entity.EntityRemoveFromWorldEvent;
import com.paragon.api.util.Wrapper;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Surge
 */
public class PopManager implements Wrapper {

    private final Map<EntityPlayer, Integer> pops = new HashMap<>();

    public PopManager() {
        MinecraftForge.EVENT_BUS.register(this);
        Paragon.INSTANCE.getEventBus().register(this);
    }

    @Listener
    public void onPacketReceive(PacketEvent.PreReceive event) {
        if (event.getPacket() instanceof SPacketEntityStatus && ((SPacketEntityStatus) event.getPacket()).getOpCode() == 35 && ((SPacketEntityStatus) event.getPacket()).getEntity(mc.world) instanceof EntityPlayer) {
            SPacketEntityStatus packet = (SPacketEntityStatus) event.getPacket();

            pops.put((EntityPlayer) packet.getEntity(mc.world), pops.containsKey((EntityPlayer) packet.getEntity(mc.world)) ? pops.get((EntityPlayer) packet.getEntity(mc.world)) + 1 : 1);

            TotemPopEvent totemPopEvent = new TotemPopEvent((EntityPlayer) ((SPacketEntityStatus) event.getPacket()).getEntity(mc.world));
            Paragon.INSTANCE.getEventBus().post(totemPopEvent);
        }
    }

    @Listener
    public void onEntityRemove(EntityRemoveFromWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            if (pops.containsKey((EntityPlayer) event.getEntity())) {
                PlayerDeathEvent playerDeathEvent = new PlayerDeathEvent((EntityPlayer) event.getEntity(), getPops((EntityPlayer) event.getEntity()));
                Paragon.INSTANCE.getEventBus().post(playerDeathEvent);

                pops.remove((EntityPlayer) event.getEntity());
            }
        }
    }

    public int getPops(EntityPlayer player) {
        return pops.getOrDefault(player, 0);
    }

}
