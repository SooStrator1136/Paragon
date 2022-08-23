package com.paragon.client.systems.module.impl.movement

import com.paragon.api.event.network.PacketEvent.PostSend
import com.paragon.api.event.network.PacketEvent.PreSend
import com.paragon.api.event.world.PlayerCollideWithBlockEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.bus.listener.Listener
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketClickWindow
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraftforge.client.event.InputUpdateEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * @author Surge, aesthetical
 */
object NoSlow : Module("NoSlow", Category.MOVEMENT, "Stop certain blocks and actions from slowing you down") {

    private val soulSand = Setting("SoulSand", true, null, null, null) describedBy "Stop soul sand from slowing you down"
    private val slime = Setting("Slime", true, null, null, null) describedBy "Stop slime blocks from slowing you down"
    private val items = Setting("Items", true, null, null, null) describedBy "Stop items from slowing you down"
    private val ncpStrict = Setting("NCPStrict", false, null, null, null) describedBy "If to bypass NCP strict checks"

    private var sneakState = false
    private var sprintState = false

    override fun onDisable() {
        super.onDisable()
        if (minecraft.anyNull) {
            return
        }

        if (sneakState && !minecraft.player.isSneaking) {
            minecraft.player.connection.sendPacket(CPacketEntityAction(minecraft.player, CPacketEntityAction.Action.STOP_SNEAKING))
            sneakState = false
        }

        if (sprintState && minecraft.player.isSprinting) {
            minecraft.player.connection.sendPacket(CPacketEntityAction(minecraft.player, CPacketEntityAction.Action.START_SPRINTING))
            sprintState = false
        }
    }

    @SubscribeEvent
    fun onInput(event: InputUpdateEvent?) {
        if (minecraft.anyNull) {
            return
        }

        if (items.value!! && minecraft.player.isHandActive && !minecraft.player.isRiding) {
            minecraft.player.movementInput.moveForward *= 5
            minecraft.player.movementInput.moveStrafe *= 5

            if (ncpStrict.value!!) {
                // funny NCP bypass - good job ncp devs
                minecraft.player.connection.sendPacket(CPacketHeldItemChange(minecraft.player.inventory.currentItem))
            }
        }
    }

    @Listener
    fun onCollideWithBlock(event: PlayerCollideWithBlockEvent) {
        if (event.blockType === Blocks.SOUL_SAND && soulSand.value!! || event.blockType === Blocks.SLIME_BLOCK && slime.value!!) {
            event.cancel()
        }
    }

    @Listener
    fun onPacketSendPre(event: PreSend) {
        if (event.packet is CPacketClickWindow && ncpStrict.value!!) {

            // i love ncp updated devs - the inventory checks are almost as good as verus's
            if (!minecraft.player.isSneaking) {
                sneakState = true
                minecraft.player.connection.sendPacket(CPacketEntityAction(minecraft.player, CPacketEntityAction.Action.START_SNEAKING))
            }

            if (minecraft.player.isSprinting) {
                sprintState = true
                minecraft.player.connection.sendPacket(CPacketEntityAction(minecraft.player, CPacketEntityAction.Action.STOP_SPRINTING))
            }
        }
    }

    @Listener
    fun onPacketSendPost(event: PostSend) {
        if (event.packet is CPacketClickWindow && ncpStrict.value!!) {

            // reset states
            if (sneakState && !minecraft.player.isSneaking) {
                minecraft.player.connection.sendPacket(CPacketEntityAction(minecraft.player, CPacketEntityAction.Action.STOP_SNEAKING))
                sneakState = false
            }

            if (sprintState && minecraft.player.isSprinting) {
                minecraft.player.connection.sendPacket(CPacketEntityAction(minecraft.player, CPacketEntityAction.Action.START_SPRINTING))
                sprintState = false
            }
        }
    }

}