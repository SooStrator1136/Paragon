package com.paragon.api.event

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import com.paragon.Paragon
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import com.paragon.api.event.network.PacketEvent.PreReceive
import net.minecraft.network.play.server.SPacketPlayerListItem
import net.minecraft.network.play.server.SPacketPlayerListItem.AddPlayerData
import com.paragon.api.event.network.PlayerEvent.PlayerJoinEvent
import com.paragon.api.event.network.PlayerEvent.PlayerLeaveEvent
import com.paragon.api.module.Module
import com.paragon.api.util.Wrapper
import me.wolfsurge.cerauno.listener.Listener
import net.minecraftforge.common.MinecraftForge
import java.util.function.Consumer

class EventFactory : Wrapper {

    @SubscribeEvent
    fun onTick(event: ClientTickEvent?) {
        Paragon.INSTANCE.moduleManager!!.modules.forEach(Consumer { module: Module ->
            if (module.isEnabled) {
                module.onTick()
            }
            if (module.bind.getValue().isPressed && Minecraft.getMinecraft().currentScreen == null) {
                Paragon.INSTANCE.eventBus.unregister(module)
                module.toggle()
            }
        })
    }

    @SubscribeEvent
    fun onRender2D(event: RenderGameOverlayEvent) {
        if (event.type.equals(RenderGameOverlayEvent.ElementType.TEXT)) {
            Paragon.INSTANCE.moduleManager!!.modules.forEach(Consumer { module: Module ->
                if (module.isEnabled) {
                    module.onRender2D()
                }
            })
        }
    }

    @SubscribeEvent
    fun onRender3D(event: RenderWorldLastEvent?) {
        Paragon.INSTANCE.moduleManager!!.modules.forEach(Consumer { module: Module ->
            if (module.isEnabled) {
                module.onRender3D()
            }
        })
    }

    @Listener
    fun onPacketReceive(event: PreReceive) {
        if (event.packet is SPacketPlayerListItem) {
            val packet = event.packet as SPacketPlayerListItem
            when (packet.action) {
                SPacketPlayerListItem.Action.ADD_PLAYER -> packet.entries.forEach(Consumer { entry: AddPlayerData ->
                    Paragon.INSTANCE.eventBus.post(
                        PlayerJoinEvent(entry.profile.name)
                    )
                })

                SPacketPlayerListItem.Action.REMOVE_PLAYER -> packet.entries.forEach(Consumer { entry: AddPlayerData ->
                    Paragon.INSTANCE.eventBus.post(
                        PlayerLeaveEvent(entry.profile.name)
                    )
                })

                else -> {}
            }
        }
    }

    init {
        MinecraftForge.EVENT_BUS.register(this)
        Paragon.INSTANCE.eventBus.register(this)
    }
}