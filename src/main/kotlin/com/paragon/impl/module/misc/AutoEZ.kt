package com.paragon.impl.module.misc

import com.paragon.impl.event.network.PacketEvent.PreSend
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category
import com.paragon.util.anyNull
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author Surge
 */
object AutoEZ : Module("AutoEZ", Category.MISC, "Automatically sends a message when you kill an opponent") {

    private val maximumRange = Setting(
        "MaxRange", 10.0, 1.0, 20.0, 0.1
    ) describedBy "The furthest distance from the player to target"

    @JvmStatic
    fun addTarget(name: String) {
        if (!targeted.contains(minecraft.world.getPlayerEntityByName(name))) {
            targeted.add(minecraft.world.getPlayerEntityByName(name))
        }
    }

    // List of targeted players
    private val targeted: MutableList<EntityPlayer?> = CopyOnWriteArrayList()

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        targeted.removeIf { it!!.getDistance(minecraft.player) > maximumRange.value }

        // Iterate through entities
        targeted.forEach {
            if ((it ?: return@forEach).health <= 0 && targeted.contains(it)) {
                minecraft.player.sendChatMessage(it.name + ", did you really just die to the worst client?!")
                targeted.remove(it)
            }
        }
    }

    @Listener
    fun onPacketSent(event: PreSend) {
        if (event.packet is CPacketUseEntity) {
            val packet = event.packet
            if (packet.action == CPacketUseEntity.Action.ATTACK) {
                if (packet.getEntityFromWorld(minecraft.world) is EntityPlayer) {
                    addTarget((packet.getEntityFromWorld(minecraft.world) as EntityPlayer).name)
                }
            }
        }
    }

    @SubscribeEvent
    fun onLivingDeath(event: LivingDeathEvent) {
        if (minecraft.anyNull) {
            return
        }

        if (event.entity is EntityPlayer) {
            val player = event.entity as EntityPlayer
            if (player.health <= 0 && targeted.contains(player)) {
                minecraft.player.sendChatMessage(player.name + ", did you really just die to the worst client?!")
                targeted.remove(player)
            }
        }
    }

}