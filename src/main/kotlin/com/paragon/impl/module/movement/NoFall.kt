package com.paragon.impl.module.movement

import com.paragon.impl.event.network.PacketEvent.PreSend
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category
import com.paragon.mixins.accessor.ICPacketPlayer
import com.paragon.mixins.accessor.IPlayerControllerMP
import com.paragon.util.anyNull
import com.paragon.util.player.InventoryUtil.getItemInHotbar
import com.paragon.util.player.InventoryUtil.switchToSlot
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.EnumHand
import net.minecraft.world.GameType

/**
 * @author Surge
 */
object NoFall : Module("NoFall", Category.MOVEMENT, "Disables fall damage") {

    private val mode = Setting("Mode", Mode.VANILLA) describedBy "How to prevent fall damage"
    private val spoofFall = Setting(
        "SpoofFall", false
    ) describedBy "Spoof fall distance" visibleWhen { mode.value == Mode.RUBBERBAND }
    private val ignoreElytra = Setting(
        "IgnoreElytra", true
    ) describedBy "Don't attempt to place a water bucket when flying with an elytra" visibleWhen { mode.value == Mode.BUCKET }

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        // Ignore if we are flying with an elytra, or we are in creative mode
        @Suppress("IncorrectFormatting") if (minecraft.player.isElytraFlying && ignoreElytra.value || minecraft.playerController.currentGameType.equals(GameType.CREATIVE)) {
            return
        }

        // We are going to take damage from falling, and we aren't over water
        if (minecraft.player.fallDistance > 3 && !minecraft.player.isOverWater) {
            when (mode.value) {
                Mode.VANILLA -> minecraft.player.connection.sendPacket(CPacketPlayer(true)) // Send a packet that says that we are on the ground

                Mode.RUBBERBAND -> {
                    // Send an invalid packet
                    minecraft.player.connection.sendPacket(
                        CPacketPlayer.Position(
                            minecraft.player.motionX, 0.0, minecraft.player.motionZ, true
                        )
                    )

                    // Set the fall distance to 0
                    if (spoofFall.value) {
                        minecraft.player.fallDistance = 0f
                    }
                }

                Mode.BUCKET ->
                    // Don't do anything if we don't have a water bucket
                    if (getItemInHotbar(Items.WATER_BUCKET) != -1) {
                        // Switch to water bucket
                        switchToSlot(getItemInHotbar(Items.WATER_BUCKET), false)

                        // Sync
                        (minecraft.playerController as IPlayerControllerMP).hookSyncCurrentPlayItem()

                        // Send rotation packet
                        minecraft.player.connection.sendPacket(
                            CPacketPlayer.Rotation(
                                minecraft.player.rotationYaw, 90f, false
                            )
                        )

                        // Set client rotation
                        minecraft.player.rotationPitch = 90f

                        // Attempt to place water bucket
                        minecraft.playerController.processRightClick(
                            minecraft.player, minecraft.world, EnumHand.MAIN_HAND
                        )
                    }

                else -> {}
            }
        }
    }

    @Listener
    fun onPacketSent(event: PreSend) {
        if (minecraft.anyNull) {
            return
        }

        // Ignore if we are flying with an elytra, or we are in creative mode
        @Suppress("IncorrectFormatting") if (minecraft.player.isElytraFlying && ignoreElytra.value || minecraft.playerController.currentGameType.equals(GameType.CREATIVE)) {
            return
        }

        // Packet is a CPacketPlayer
        if (event.packet is CPacketPlayer) {
            if (mode.value == Mode.PACKET_MODIFY) {
                // Set packet Y
                (event.packet as ICPacketPlayer).hookSetY(minecraft.player.posY + 1)

                // Set packet onGround
                (event.packet as ICPacketPlayer).hookSetOnGround(true)
            }
        }
    }

    enum class Mode {
        /**
         * One simple packet to negate fall damage
         */
        VANILLA,

        /**
         * Invalid packet to lag us back
         */
        RUBBERBAND,

        /**
         * Modify packet about to be sent to the server
         */
        PACKET_MODIFY,

        /**
         * Attempt to place water bucket beneath us
         */
        BUCKET
    }

}