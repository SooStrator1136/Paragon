package com.paragon.api.event;

import com.paragon.Paragon;
import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.event.combat.TotemPopEvent;
import com.paragon.api.event.network.PlayerEvent;
import com.paragon.api.util.Wrapper;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraftforge.common.MinecraftForge;

public class EventParser implements Wrapper {

    public EventParser() {
        MinecraftForge.EVENT_BUS.register(this);
        Paragon.INSTANCE.getEventBus().register(this);
    }

    @Listener
    public void onPacketReceive(PacketEvent.PreReceive event) {
        if (event.getPacket() instanceof SPacketPlayerListItem) {
            SPacketPlayerListItem packet = (SPacketPlayerListItem) event.getPacket();

            switch (packet.getAction()) {
                case ADD_PLAYER:
                    packet.getEntries().forEach(entry -> {
                        Paragon.INSTANCE.getEventBus().post(new PlayerEvent.PlayerJoinEvent(entry.getProfile().getName()));
                    });

                    break;

                case REMOVE_PLAYER:
                    packet.getEntries().forEach(entry -> {
                        Paragon.INSTANCE.getEventBus().post(new PlayerEvent.PlayerLeaveEvent(entry.getProfile().getName()));
                    });

                    break;
            }
        }
    }

}
