package com.paragon.api.event;

import com.paragon.Paragon;
import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.event.combat.TotemPopEvent;
import com.paragon.api.event.network.PlayerEvent;
import com.paragon.api.module.Module;
import com.paragon.api.util.Wrapper;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventFactory implements Wrapper {

    public EventFactory() {
        MinecraftForge.EVENT_BUS.register(this);
        Paragon.INSTANCE.getEventBus().register(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        Paragon.INSTANCE.getModuleManager().getModules().forEach(module -> {
            if (module.isEnabled()) {
                module.onTick();
            }

            if (module.getBind().getValue().isPressed() && Minecraft.getMinecraft().currentScreen == null) {
                Paragon.INSTANCE.getEventBus().unregister(module);
                module.toggle();
            }
        });
    }

    @SubscribeEvent
    public void onRender2D(RenderGameOverlayEvent event) {
        if (event.getType().equals(RenderGameOverlayEvent.ElementType.TEXT)) {
            Paragon.INSTANCE.getModuleManager().getModules().forEach(module -> {
                if (module.isEnabled()) {
                    module.onRender2D();
                }
            });
        }
    }

    @SubscribeEvent
    public void onRender3D(RenderWorldLastEvent event) {
        Paragon.INSTANCE.getModuleManager().getModules().forEach(module -> {
            if (module.isEnabled()) {
                module.onRender3D();
            }
        });
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
