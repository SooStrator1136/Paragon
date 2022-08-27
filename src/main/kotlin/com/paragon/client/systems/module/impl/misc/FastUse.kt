package com.paragon.client.systems.module.impl.misc

import com.paragon.api.event.network.PacketEvent.PreSend
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.player.InventoryUtil
import com.paragon.mixins.accessor.IMinecraft
import com.paragon.bus.listener.Listener
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import java.util.*

/**
 * @author Surge
 */
object FastUse : Module("FastUse", Category.MISC, "Allows you to use items quicker than you would be able to in vanilla") {

    private val xp = Setting(
        "XP",
        true
    ) describedBy "Fast use XP bottles"

    private val rotate = Setting(
        "Rotate",
        true
    ) describedBy "Rotate your player when using XP bottles" subOf xp

    private val crystals = Setting(
        "Crystals",
        true
    ) describedBy "Place crystals fast"

    private val randomPause = Setting(
        "RandomPause",
        true
    ) describedBy "Randomly pauses to try and prevent you from being kicked"

    private val randomChance = Setting(
        "Chance",
        50f,
        2f,
        100f,
        1f
    ) describedBy "The chance to pause" subOf randomPause

    private val random = Random()

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        // Check we want to set the delay timer to 0
        if (xp.value && InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE) || crystals.value && InventoryUtil.isHolding(Items.END_CRYSTAL)) {
            if (randomPause.value && random.nextInt(randomChance.value.toInt()) == 1) {
                (minecraft as IMinecraft).setRightClickDelayTimer(4)
                return
            }

            if (InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE)) {
                minecraft.player.xpCooldown = 0
            }

            (minecraft as IMinecraft).setRightClickDelayTimer(0)
        }
    }

    @Listener
    fun onPacketSend(event: PreSend) {
        if (event.packet !is CPacketPlayerTryUseItem || !InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE) || !xp.value || !rotate.value) {
            return
        }

        // Send rotation packet. We aren't using the rotation manager as it doesn't immediately rotate the player
        minecraft.player.connection.sendPacket(
            CPacketPlayer.Rotation(
                minecraft.player.rotationYaw,
                90f,
                minecraft.player.onGround
            )
        )
    }

}