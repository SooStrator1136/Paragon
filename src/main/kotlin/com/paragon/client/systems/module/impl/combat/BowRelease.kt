package com.paragon.client.systems.module.impl.combat

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.math.BlockPos

/**
 * @author Surge
 */
object BowRelease : Module("BowRelease", Category.COMBAT, "Automatically releases your bow when at max charge") {

    private val release = Setting("Release", Release.TICKS, null, null, null) describedBy "When to release the bow"
    private val releasePower = Setting("Power", 3.1f, 0.1f, 4.0f, 0.1f) describedBy "The power the bow needs to be before releasing" visibleWhen  { release.value == Release.POWER }
    private val releaseTicks = Setting("Ticks", 3.0f, 0.0f, 60.0f, 1.0f) describedBy "The amount of ticks that have passed before releasing" visibleWhen  { release.value == Release.TICKS }

    private var ticks = 0

    override fun onTick() {
        if (minecraft.anyNull || minecraft.player.heldItemMainhand.item !== Items.BOW) {
            return
        }

        if (!minecraft.player.isHandActive || minecraft.player.itemInUseMaxCount < 3) {
            return
        }

        when (release.value) {
            Release.POWER -> {
                // Get the charge power (awesome logic from trajectories!)
                val power: Float = ((72000 - minecraft.player.itemInUseCount) / 20.0f * ((72000 - minecraft.player.itemInUseCount) / 20.0f) + (72000 - minecraft.player.itemInUseCount) / 20.0f * 2.0f) / 3.0f * 3

                // Return if the power is not high enough
                if (power < releasePower.value) {
                    return
                }
            }

            Release.TICKS ->
                // Return if we haven't passed the required ticks
                if (ticks++ < releaseTicks.value) {
                    return
                }

            else -> {}
        }


        // Release the bow
        minecraft.player.connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, minecraft.player.horizontalFacing))
        minecraft.player.connection.sendPacket(CPacketPlayerTryUseItem(minecraft.player.activeHand))
        minecraft.player.stopActiveHand()

        // Set ticks back to 0
        ticks = 0
    }

    enum class Release {
        /**
         * Release on specified power
         */
        POWER,

        /**
         * Release on amount of ticks
         */
        TICKS
    }

}