package com.paragon.impl.module.combat

import com.paragon.impl.event.network.PacketEvent
import com.paragon.impl.module.Module
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketUseEntity

/**
 * @author Surge
 */
object Criticals : Module("Criticals", Category.COMBAT, "Makes all your hits critical hits") {

    @Listener
    fun onPacketSend(event: PacketEvent.PreSend) {
        //We are attacking an entity
        if (event.packet is CPacketUseEntity) {

            // Check the packets action and if the entity we are attacking is a living entity
            if (event.packet.action != CPacketUseEntity.Action.ATTACK || event.packet.getEntityFromWorld(minecraft.world) !is EntityLivingBase) {
                return
            }

            // We are on the ground and we aren't jumping
            if (minecraft.player.onGround && !minecraft.gameSettings.keyBindJump.isKeyDown) {
                // Send packets
                minecraft.player.connection.sendPacket(
                    CPacketPlayer.Position(
                        minecraft.player.posX, minecraft.player.posY + 0.1, minecraft.player.posZ, false
                    )
                )
                minecraft.player.connection.sendPacket(
                    CPacketPlayer.Position(
                        minecraft.player.posX, minecraft.player.posY, minecraft.player.posZ, false
                    )
                )
            }
        }
    }

}