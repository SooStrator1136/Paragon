package com.paragon.client.systems.module.impl.combat

import com.paragon.Paragon
import com.paragon.api.event.network.PacketEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.player.InventoryUtil
import com.paragon.api.util.player.PlacementUtil
import com.paragon.api.util.player.RotationUtil
import com.paragon.api.util.render.ColourUtil.integrateAlpha
import com.paragon.api.util.render.builder.RenderBuilder
import com.paragon.api.util.world.BlockUtil
import com.paragon.api.util.world.BlockUtil.getBlockAtPos
import com.paragon.bus.listener.Listener
import com.paragon.client.managers.notifications.Notification
import com.paragon.client.managers.notifications.NotificationType
import com.paragon.client.managers.rotation.Rotate
import com.paragon.client.managers.rotation.Rotation
import com.paragon.client.managers.rotation.RotationPriority
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketBlockChange
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.floor

/**
 * @author Surge
 * @since 23/08/2022
 */
object Surround : Module("Surround", Category.COMBAT, "Automatically surrounds you with obsidian") {

    private val performOn = Setting("PerformOn", PerformOn.PACKET) describedBy "When to perform"
    private val disable = Setting("Disable", Disable.OFF_GROUND) describedBy "When to automatically disable the module"
    private val center = Setting("Center", Center.MOTION) describedBy "How to center the player to the center of the block"
    private val blocksPerTick = Setting("BlocksPerTick", 4.0, 1.0, 8.0, 1.0) describedBy "The limit to how many blocks can be placed in a tick"
    private val support = Setting("Support", true) describedBy "Support blocks by placing beneath them"

    private val rotate = Setting("Rotate", Rotate.PACKET) describedBy "How to rotate"

    private val render = Setting("Render", true) describedBy "Render a highlight on the positions we need to place blocks at"
    private val renderColour = Setting("Colour", Color(185, 17, 255, 130)) describedBy "The colour of the highlight" subOf render

    // List of positions to place on tick
    private var surroundPositions = arrayListOf<BlockPos>()

    // This is to prevent us from attempting placement multiple times
    private val placedCache = CopyOnWriteArrayList<BlockPos>()

    override fun onEnable() {
        if (minecraft.anyNull) {
            return
        }

        // No obsidian to place
        if (InventoryUtil.getHotbarBlockSlot(Blocks.OBSIDIAN) == -1) {
            Paragon.INSTANCE.notificationManager.addNotification(Notification("No obsidian in hotbar!", NotificationType.ERROR))
            toggle()
            return
        }

        when (center.value) {
            Center.MOTION -> {
                // Set player's motion to walk to the center of the block
                minecraft.player.motionX = (MathHelper.floor(minecraft.player.posX) + 0.5 - minecraft.player.posX) / 2
                minecraft.player.motionZ = (MathHelper.floor(minecraft.player.posZ) + 0.5 - minecraft.player.posZ) / 2
            }

            Center.SNAP -> {
                // Send movement packet
                minecraft.player.connection.sendPacket(CPacketPlayer.Position(MathHelper.floor(minecraft.player.posX) + 0.5, minecraft.player.posY, MathHelper.floor(minecraft.player.posZ) + 0.5, minecraft.player.onGround))

                // Set position client-side
                minecraft.player.setPosition(MathHelper.floor(minecraft.player.posX) + 0.5, minecraft.player.posY, MathHelper.floor(minecraft.player.posZ) + 0.5)
            }

            else -> {}
        }

        surroundPositions = getBlocks()
    }

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        // Remove if non-replaceable block
        placedCache.removeIf { isNotReplaceable(it) }

        // Disable

        // No obsidian to place
        if (InventoryUtil.getHotbarBlockSlot(Blocks.OBSIDIAN) == -1) {
            Paragon.INSTANCE.notificationManager.addNotification(Notification("No obsidian in hotbar!", NotificationType.ERROR))
            toggle()
            return
        }

        if (surroundPositions.isEmpty() && disable.value == Disable.FINISHED) {
            Paragon.INSTANCE.notificationManager.addNotification(Notification("Surround Finished, Disabling!", NotificationType.INFO))
            toggle()
            return
        }

        if (!minecraft.player.onGround && disable.value == Disable.OFF_GROUND) {
            Paragon.INSTANCE.notificationManager.addNotification(Notification("Player is no longer on ground, disabling!", NotificationType.INFO))
            toggle()
            return
        }

        // Refresh blocks on tick
        if (performOn.value == PerformOn.TICK) {
            surroundPositions = getBlocks()
        }

        if (surroundPositions.isNotEmpty()) {
            // List of positions to place at
            val placePositions = ArrayList<BlockPos>()

            // Add all positions from index 0 to the max blocks per tick
            for (i in 0..MathHelper.clamp(surroundPositions.size - 1, 0, blocksPerTick.value.toInt())) {
                placePositions.add(surroundPositions[i])
            }

            // Place blocks
            for (pos in placePositions) {
                placeOnPosition(pos)
            }

            // Remove placed positions from global list
            surroundPositions.removeAll(placePositions.toSet())
        }
    }

    @Listener
    fun onPacketReceived(event: PacketEvent.PreReceive) {
        if (event.packet is SPacketBlockChange && performOn.value == PerformOn.PACKET) {
            val pos = event.packet.blockPosition

            // It's a placeable position, and we haven't already attempted to place there
            if (!isNotReplaceable(pos) && !placedCache.contains(pos)) {
                // Check sub (support) status
                val sub = if (support.value && pos.down().getBlockAtPos().isReplaceable(minecraft.world, pos.down())) pos.down() else null

                // Place sub block
                if (sub != null) {
                    placeOnPosition(sub)
                }

                // Place origin block
                placeOnPosition(pos)

                // Otherwise we seem to attempt placing 2-3 times
                placedCache.add(pos)
            }
        }
    }

    override fun onRender3D() {
        surroundPositions.forEach {
            RenderBuilder()
                .boundingBox(BlockUtil.getBlockBox(it))
                .inner(renderColour.value)
                .outer(renderColour.value.integrateAlpha(255f))

                .start()

                .blend(true)
                .texture(true)
                .depth(true)

                .build(false)
        }
    }

    private fun isNotReplaceable(pos: BlockPos): Boolean = !pos.getBlockAtPos().blockState.block.isReplaceable(minecraft.world, pos)

    private fun getBlocks(origin: BlockPos): ArrayList<BlockPos> {
        val blocks = arrayListOf<BlockPos>()

        // If we don't want to support, ignore this block
        if (!support.value && origin.getBlockAtPos().isReplaceable(minecraft.world, origin) && origin.down().getBlockAtPos().isReplaceable(minecraft.world, origin.down())) {
            return blocks
        }

        // Check that the origin block is replaceable
        if (origin.getBlockAtPos().isReplaceable(minecraft.world, origin)) {
            // If we want to support, and the block below is replaceable, add it to the list
            if (support.value && origin.down().getBlockAtPos().isReplaceable(minecraft.world, origin.down())) {
                blocks.add(origin.down())
            }

            // Add origin
            blocks.add(origin)
        }

        return blocks
    }

    private fun getBlocks(): ArrayList<BlockPos> {
        val blocks = ArrayList<BlockPos>()
        val playerPos = BlockPos(floor(minecraft.player.posX), floor(minecraft.player.posY), floor(minecraft.player.posZ)).add(0, 1, 0)

        // Add blocks
        blocks.addAll(getBlocks(playerPos.add(-1, -1, 0)))
        blocks.addAll(getBlocks(playerPos.add(1, -1, 0)))
        blocks.addAll(getBlocks(playerPos.add(0, -1, -1)))
        blocks.addAll(getBlocks(playerPos.add(0, -1, 1)))

        return blocks
    }

    private fun placeOnPosition(position: BlockPos) {
        // Get current item
        val slot: Int = minecraft.player.inventory.currentItem

        // Slot to switch to
        val obsidianSlot = InventoryUtil.getHotbarBlockSlot(Blocks.OBSIDIAN)

        if (obsidianSlot != -1) {
            minecraft.player.inventory.currentItem = obsidianSlot

            // Get rotation yaw and pitch
            val rotationValues = RotationUtil.getRotationToBlockPos(position, 0.5)

            // Place
            PlacementUtil.place(position, Rotation(rotationValues.x, rotationValues.y, rotate.value, RotationPriority.HIGH))

            // Reset slot to our original slot
            minecraft.player.inventory.currentItem = slot
        }
    }

    enum class PerformOn {
        /**
         * Place when block is destroyed
         */
        PACKET,

        /**
         * Place on tick
         */
        TICK
    }

    enum class Disable {
        /**
         * Disable when finished
         */
        FINISHED,

        /**
         * Disable when off ground
         */
        OFF_GROUND,

        /**
         * Never disable
         */
        NEVER
    }

    enum class Center {
        /**
         * Move the player to the center of the block
         */
        MOTION,

        /**
         * Snap the player to the center of the block
         */
        SNAP,

        /**
         * Do not center the player
         */
        OFF
    }

}