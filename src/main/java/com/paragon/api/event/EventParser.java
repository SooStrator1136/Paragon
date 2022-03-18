package com.paragon.api.event;

import com.paragon.Paragon;
import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.event.combat.TotemPopEvent;
import com.paragon.api.util.Wrapper;
import com.paragon.api.util.other.IntegerReference;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraftforge.common.MinecraftForge;

public class EventParser implements Wrapper {

    public EventParser() {
        MinecraftForge.EVENT_BUS.register(this);
        Paragon.INSTANCE.getEventBus().register(this);
    }

    @Listener
    public void onPacketReceive(PacketEvent.PreReceive event) {
        if (event.getPacket() instanceof SPacketEntityStatus && ((SPacketEntityStatus) event.getPacket()).getOpCode() == IntegerReference.TOTEM_POP_CODE && ((SPacketEntityStatus) event.getPacket()).getEntity(mc.world) instanceof EntityPlayer) {
            TotemPopEvent totemPopEvent = new TotemPopEvent((EntityPlayer) ((SPacketEntityStatus) event.getPacket()).getEntity(mc.world));
            Paragon.INSTANCE.getEventBus().post(totemPopEvent);
        }
    }

}
