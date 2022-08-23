package com.paragon.client.managers

import com.paragon.Paragon
import com.paragon.api.event.combat.PlayerDeathEvent
import com.paragon.api.event.combat.TotemPopEvent
import com.paragon.api.event.network.PacketEvent.PreReceive
import com.paragon.api.event.world.entity.EntityRemoveFromWorldEvent
import com.paragon.api.util.Wrapper
import com.paragon.bus.listener.Listener
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.SPacketEntityStatus

/**
 * @author Surge
 */
class PopManager : Wrapper {

    private val pops: MutableMap<EntityPlayer, Int> = HashMap()

    init {
        Paragon.INSTANCE.eventBus.register(this)
    }

    @Listener
    fun onPacketReceive(event: PreReceive) {
        if (event.packet is SPacketEntityStatus && event.packet.opCode.toInt() == 35 && (event.packet as SPacketEntityStatus).getEntity(minecraft.world) is EntityPlayer) {
            val packet = event.packet
            pops[packet.getEntity(minecraft.world) as EntityPlayer] = if (pops.containsKey(packet.getEntity(minecraft.world) as EntityPlayer)) pops[packet.getEntity(minecraft.world) as EntityPlayer]!! + 1 else 1

            val totemPopEvent = TotemPopEvent((event.packet.getEntity(minecraft.world) as EntityPlayer))
            Paragon.INSTANCE.eventBus.post(totemPopEvent)
        }
    }

    @Listener
    fun onEntityRemove(event: EntityRemoveFromWorldEvent) {
        if (event.entity is EntityPlayer) {
            if (pops.containsKey(event.entity)) {
                val playerDeathEvent = PlayerDeathEvent(event.entity, getPops(event.entity))
                Paragon.INSTANCE.eventBus.post(playerDeathEvent)

                pops.remove(event.entity)
            }
        }
    }

    fun getPops(player: EntityPlayer): Int {
        return pops.getOrDefault(player, 0)
    }

}