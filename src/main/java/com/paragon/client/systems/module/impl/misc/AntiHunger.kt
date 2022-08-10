package com.paragon.client.systems.module.impl.misc

import com.paragon.api.event.network.PacketEvent.PostSend
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.asm.mixins.accessor.ICPacketPlayer
import me.wolfsurge.cerauno.listener.Listener
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer

object AntiHunger :
    Module("AntiHunger", Category.MISC, "Tries to remove huger lost") {

    private val groundSpoof = Setting("GroundSpoof", true) describedBy "Spoofs your onGround state"
    private val antiSprint = Setting("AntiSprint", true) describedBy "Kassuk tries to code"

    private var previousSprint = false



    override fun onEnable() {
        if (minecraft.player.isSprinting) {
            previousSprint = true
            minecraft.player.connection.sendPacket(
                CPacketEntityAction(
                    minecraft.player, CPacketEntityAction.Action.STOP_SPRINTING
                )
            )
        }
        super.onEnable()
    }

    override fun onDisable() {
        super.onDisable()
        if (previousSprint) {
            previousSprint = false
            minecraft.player.connection.sendPacket(
                CPacketEntityAction(
                    minecraft.player,
                    CPacketEntityAction.Action.START_SPRINTING
                )
            )
        }
    }

    @Listener
    fun onPacketSend(event: PostSend ) {
        if (event.packet is CPacketPlayer) {
            if (groundSpoof.value) {
                if (!minecraft.player.isRiding && !minecraft.player.isElytraFlying) {
                    (event.packet as ICPacketPlayer).setOnGround(true)
                }
            }
            //Kassuk part
        } else if (event.packet is CPacketEntityAction) {
            val packet = event.packet
            if (packet.action.equals(CPacketEntityAction.Action.START_SPRINTING) || packet.action.equals(CPacketEntityAction.Action.STOP_SPRINTING)
            ) {
                if (antiSprint.value) {
                    event.isCancelled()
                }
            }
        }
    }
}
