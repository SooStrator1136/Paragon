package com.paragon.impl.event

import com.paragon.Paragon
import com.paragon.impl.event.network.PacketEvent.PreReceive
import com.paragon.impl.event.network.PlayerEvent.PlayerJoinEvent
import com.paragon.impl.event.network.PlayerEvent.PlayerLeaveEvent
import com.paragon.bus.listener.Listener
import com.paragon.util.Wrapper
import net.minecraft.client.Minecraft
import net.minecraft.network.play.server.SPacketPlayerListItem
import net.minecraft.network.play.server.SPacketPlayerListItem.AddPlayerData
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

class EventFactory : Wrapper {

    @SubscribeEvent
    fun onTick(event: ClientTickEvent?) {
        Paragon.INSTANCE.moduleManager.modules.forEach {
            if (it.isEnabled) {
                it.onTick()
            }

            if (it.bind.value.isPressed() && Minecraft.getMinecraft().currentScreen == null) {
                Paragon.INSTANCE.eventBus.unregister(it)
                it.toggle()
            }
        }
    }

    @SubscribeEvent
    fun onRender2D(event: RenderGameOverlayEvent) {
        if (event.type.equals(RenderGameOverlayEvent.ElementType.TEXT)) {
            Paragon.INSTANCE.moduleManager.modules.forEach {
                if (it.isEnabled) {
                    it.onRender2D()
                }
            }
        }
    }

    @SubscribeEvent
    fun onRender3D(event: RenderWorldLastEvent?) {
        Paragon.INSTANCE.moduleManager.modules.forEach {
            if (it.isEnabled) {
                it.onRender3D()
            }
        }
    }

    @Listener
    fun onPacketReceive(event: PreReceive) {
        if (event.packet is SPacketPlayerListItem) {
            val packet = event.packet

            when (packet.action) {
                SPacketPlayerListItem.Action.ADD_PLAYER -> packet.entries.forEach { entry: AddPlayerData ->
                    if (entry.profile.name != null) {
                        Paragon.INSTANCE.eventBus.post(
                            PlayerJoinEvent(entry.profile.name)
                        )
                    }
                }

                SPacketPlayerListItem.Action.REMOVE_PLAYER -> packet.entries.forEach { entry: AddPlayerData ->
                    if (entry.profile.name != null) {
                        Paragon.INSTANCE.eventBus.post(
                            PlayerLeaveEvent(entry.profile.name)
                        )
                    }
                }

                else -> {}
            }
        }
    }

    init {
        MinecraftForge.EVENT_BUS.register(this)
        Paragon.INSTANCE.eventBus.register(this)
    }

}