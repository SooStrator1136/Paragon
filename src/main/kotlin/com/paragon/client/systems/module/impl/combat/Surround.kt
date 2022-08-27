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
    private val airPlace = Setting("AirPlace", AirPlace.SUPPORT) describedBy "How to interact with air blocks"
    private val floor = Setting("Floor", true) describedBy "Allows you to place on the floor"

    private val rotate = Setting("Rotate", Rotate.PACKET) describedBy "How to rotate"
    private val reset = Setting("Reset", true) describedBy "Reset your rotation after placing" visibleWhen { rotate.value == Rotate.LEGIT }

    private val render = Setting("Render", true) describedBy "Render a highlight on the positions we need to place blocks at"
    private val renderColour = Setting("Colour", Color(185, 17, 255, 130)) describedBy "The colour of the highlight" subOf render

    private val surroundPositions = arrayListOf<BlockPos>()
    private var enablePositions = arrayListOf<BlockPos>()

    override fun onEnable() {
        if (minecraft.anyNull) {
            return
        }

        // No obsidian to place
        if (InventoryUtil.getHotbarBlockSlot(Blocks.OBSIDIAN) == -1) {
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

        surroundPositions.clear()
        enablePositions.clear()

        enablePositions = getBlocks()
    }

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        surroundPositions.removeIf { isNotReplaceable(it) || !BlockUtil.getSphere(1f, false).contains(it) }

        if (surroundPositions.isEmpty() && enablePositions.isEmpty() && disable.value == Disable.FINISHED) {
            Paragon.INSTANCE.notificationManager.addNotification(Notification("Surround Finished, Disabling!", NotificationType.INFO))
            toggle()
        }

        if (!minecraft.player.onGround && disable.value == Disable.OFF_GROUND) {
            Paragon.INSTANCE.notificationManager.addNotification(Notification("Player is no longer on ground, disabling!", NotificationType.INFO))
            toggle()
        }

        if (enablePositions.isNotEmpty()) {
            val placePositions = ArrayList<BlockPos>()

            var i = 0
            for (pos in enablePositions) {
                i++

                placePositions.add(pos)
            }

            for (pos in placePositions) {
                placeOnPosition(pos)
            }

            enablePositions.removeAll(enablePositions.toSet())
        }
    }

    @Listener
    fun onPacketReceived(event: PacketEvent.PreReceive) {
        if (event.packet is SPacketBlockChange) {
            if (event.packet.blockPosition.getBlockAtPos().isReplaceable(minecraft.world, event.packet.blockPosition) && isSurroundPosition(event.packet.blockPosition)) {
                if (performOn.value == PerformOn.PACKET) {
                    val pos = event.packet.blockPosition
                    val sub = if (airPlace.value == AirPlace.SUPPORT && pos.down().getBlockAtPos().isReplaceable(minecraft.world, pos.down())) pos.down() else null
                    
                    if (sub != null) {
                        placeOnPosition(sub)
                    }

                    placeOnPosition(pos)
                }
            }
        }
    }

    override fun onRender3D() {
        enablePositions.forEach {
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

    private fun isSurroundPosition(pos: BlockPos): Boolean {
        val playerPosition = minecraft.player.position

        return (pos.x == playerPosition.x - 1 || pos.x == playerPosition.x + 1 || pos.z == playerPosition.z - 1 || pos.z == playerPosition.z + 1) && pos.y == playerPosition.y
    }

    private fun isNotReplaceable(pos: BlockPos): Boolean = !pos.getBlockAtPos().blockState.block.isReplaceable(minecraft.world, pos)

    private fun getBlocks(origin: BlockPos): ArrayList<BlockPos> {
        val blocks = arrayListOf<BlockPos>()

        if (origin.down().getBlockAtPos().isReplaceable(minecraft.world, origin.down()) && airPlace.value == AirPlace.SUPPORT) {
            blocks.add(origin.down().down())
        }

        blocks.add(origin.down())

        return blocks
    }

    private fun getBlocks(): ArrayList<BlockPos> {
        val blocks = ArrayList<BlockPos>()
        val playerPos = BlockPos(floor(minecraft.player.posX), floor(minecraft.player.posY), floor(minecraft.player.posZ)).add(0, 1, 0)

        blocks.addAll(getBlocks(playerPos.add(-1, 0, 0)))
        blocks.addAll(getBlocks(playerPos.add(1, 0, 0)))
        blocks.addAll(getBlocks(playerPos.add(0, 0, -1)))
        blocks.addAll(getBlocks(playerPos.add(0, 0, 1)))

        return blocks
    }

    private fun placeOnPosition(position: BlockPos) {
        // Get current item
        val slot: Int = minecraft.player.inventory.currentItem

        // Slot to switch to
        val obsidianSlot = InventoryUtil.getHotbarBlockSlot(Blocks.OBSIDIAN)

        if (obsidianSlot != -1) {
            val rotationValues = RotationUtil.getRotationToBlockPos(position, 0.5)

            PlacementUtil.place(position, Rotation(rotationValues.x, rotationValues.y, rotate.value, RotationPriority.HIGH))

            // Reset slot to our original slot
            minecraft.player.inventory.currentItem = slot
        }
    }

    private fun place(position: Position) {
        val original = Vec2f(minecraft.player.rotationYaw, minecraft.player.rotationPitch)

        // Get rotation
        val rotation = RotationUtil.getRotationToBlockPos(position.blockPos, 0.5)

        // Rotate to position
        if (rotate.value == Rotate.LEGIT) {
            minecraft.player.rotationYaw = rotation.x
            minecraft.player.rotationPitch = rotation.y
            minecraft.player.rotationYawHead = rotation.x
        }

        if (rotate.value != Rotate.NONE) {
            minecraft.player.connection.sendPacket(CPacketPlayer.Rotation(rotation.x, rotation.y, minecraft.player.onGround))
        }

        // Get current item
        val slot: Int = minecraft.player.inventory.currentItem

        // Slot to switch to
        val obsidianSlot = InventoryUtil.getHotbarBlockSlot(Blocks.OBSIDIAN)

        if (obsidianSlot != -1) {
            minecraft.player.inventory.currentItem = obsidianSlot
            minecraft.player.connection.sendPacket(CPacketEntityAction(minecraft.player, CPacketEntityAction.Action.START_SNEAKING))

            minecraft.playerController.processRightClickBlock(minecraft.player, minecraft.world, position.blockPos.offset(position.facing), position.facing.opposite, Vec3d(position.blockPos), EnumHand.MAIN_HAND)

            minecraft.player.swingArm(EnumHand.MAIN_HAND)
            minecraft.player.connection.sendPacket(CPacketEntityAction(minecraft.player, CPacketEntityAction.Action.STOP_SNEAKING))
        }

        // Reset slot to our original slot
        minecraft.player.inventory.currentItem = slot

        if (rotate.value != Rotate.NONE && (rotate.value == Rotate.PACKET || reset.value)) {
            if (rotate.value == Rotate.LEGIT) {
                minecraft.player.rotationYaw = original.x
                minecraft.player.rotationPitch = original.y
                minecraft.player.rotationYawHead = original.x
            }

            minecraft.player.connection.sendPacket(CPacketPlayer.Rotation(original.x, original.y, minecraft.player.onGround))
        }
    }

    data class Position(val blockPos: BlockPos, val facing: EnumFacing)

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

    enum class AirPlace {
        /**
         * Place below then on top
         */
        SUPPORT,

        /**
         * Place in air
         */
        AIR,

        /**
         * Do not place
         */
        IGNORE
    }

}