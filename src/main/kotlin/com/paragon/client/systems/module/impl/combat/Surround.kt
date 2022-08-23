package com.paragon.client.systems.module.impl.combat

import com.paragon.Paragon
import com.paragon.api.event.client.SettingUpdateEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.player.InventoryUtil.getHotbarBlockSlot
import com.paragon.api.util.player.RotationUtil.getRotationToBlockPos
import com.paragon.api.util.render.ColourUtil.integrateAlpha
import com.paragon.api.util.render.builder.BoxRenderMode
import com.paragon.api.util.render.builder.RenderBuilder
import com.paragon.api.util.world.BlockUtil.getBlockAtPos
import com.paragon.api.util.world.BlockUtil.getBlockBox
import com.paragon.bus.listener.Listener
import com.paragon.client.managers.rotation.Rotate
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.*
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.awt.Color

/**
 * @author Surge
 * @since 08/05/2022
 *
 * @todo Rewrite this
 */
@SideOnly(Side.CLIENT)
object Surround : Module("Surround", Category.COMBAT, "Places obsidian around you to protect you from crystals") {

    // General settings
    private val disable = Setting("Disable", Disable.NEVER, null, null, null) describedBy "When to automatically disable the module"
    private val threaded = Setting("Threaded", false, null, null, null)
    private val blocksPerTick = Setting("BlocksPerTick", 4.0, 1.0, 10.0, 1.0) describedBy "The maximum amount of blocks to be placed per tick"
    private val center = Setting("Center", Center.MOTION, null, null, null) describedBy "Center the player on the block when enabled"
    private val air = Setting("Air", Air.SUPPORT, null, null, null) describedBy "Place blocks beneath where we are placing"

    // Rotate settings
    private val rotate = Setting("Rotate", Rotate.LEGIT, null, null, null) describedBy "How to rotate the player"
    private val rotateBack = Setting("RotateBack", true, null, null, null) describedBy "Rotate the player back to their original rotation" subOf rotate

    // Render
    private val render = Setting("Render", true, null, null, null) describedBy "Render a highlight on the positions we need to place blocks at"
    private val renderColour = Setting("Colour", Color(185, 17, 255, 130), null, null, null) describedBy "The colour of the highlight" subOf render

    // Map of blocks to render
    private var renderBlocks: Map<BlockPos, EnumFacing> = HashMap()
    private var placingThread: Thread? = null

    override fun onEnable() {
        if (minecraft.anyNull) {
            return
        }

        // No obsidian to place
        if (getHotbarBlockSlot(Blocks.OBSIDIAN) == -1) {
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

        if (threaded.value!!) {
            placingThread = Thread(Placer())
            placingThread!!.start()
        }
    }

    override fun onTick() {
        if (!threaded.value!!) {
            doSurround()
        }
    }

    fun doSurround() {
        if (minecraft.anyNull) {
            return
        }

        // Check we are in air
        if (disable.value == Disable.AIR && !minecraft.player.onGround) {
            toggle()
            return
        }

        // We don't have obsidian in our hotbar
        if (getHotbarBlockSlot(Blocks.OBSIDIAN) == -1) {
            Paragon.INSTANCE.commandManager.sendClientMessage(TextFormatting.RED.toString() + "No obsidian available, Surround disabled!", false)
            toggle()
            return
        }

        // Blocks we need to place
        val blocks: MutableMap<BlockPos, EnumFacing> = HashMap()

        // The player's position
        val playerPos = BlockPos(MathHelper.floor(minecraft.player.posX), MathHelper.floor(minecraft.player.posY), MathHelper.floor(minecraft.player.posZ))
        for (x in -1..1) {
            // Our X position
            if (x == 0) {
                continue
            }

            // Get position
            var pos = playerPos.add(x, 0, 0)

            // Check if we need to support the block
            if (canPlaceBlock(pos.down()) && air.value == Air.SUPPORT) {
                // Get offset
                for (facing in EnumFacing.values()) {
                    // Make sure we can place on offset
                    if (pos.down().offset(facing).getBlockAtPos() !== Blocks.AIR) {
                        // Add block
                        blocks[pos.down()] = facing
                        break
                    }
                }
            }

            // We can place the block or we are supporting it
            if (canPlaceBlock(pos) || air.value == Air.SUPPORT && canPlaceBlock(pos.down())) {
                // Get offset
                for (facing in EnumFacing.values()) {
                    // Make sure we can place on offset
                    if (pos.offset(facing).getBlockAtPos() !== Blocks.AIR) {
                        // Add block
                        blocks[pos] = facing
                        break
                    }
                }
            }
            for (z in -1..1) {
                // Our Z position
                if (z == 0) {
                    continue
                }

                // Get position
                pos = playerPos.add(0, 0, z)

                // Check if we need to support the block
                if (canPlaceBlock(pos.down()) && air.value == Air.SUPPORT) {
                    // Get offset
                    for (facing in EnumFacing.values()) {
                        // Make sure we can place on offset
                        if (pos.down().offset(facing).getBlockAtPos() !== Blocks.AIR) {
                            // Add block
                            blocks[pos.down()] = facing
                            break
                        }
                    }
                }

                // We can place the block or we are supporting it
                if (canPlaceBlock(pos) || air.value == Air.SUPPORT && canPlaceBlock(pos.down())) {
                    // Get offset
                    for (facing in EnumFacing.values()) {
                        // Make sure we can place on offset
                        if (pos.offset(facing).getBlockAtPos() !== Blocks.AIR) {
                            // Add block
                            blocks[pos] = facing
                            break
                        }
                    }
                }
            }
        }

        // Set render blocks
        renderBlocks = blocks

        // Get our original rotation
        val originalRotation = Vec2f(minecraft.player.rotationYaw, minecraft.player.rotationPitch)

        // Place blocks until either:
        // A. We run out of blocks
        // B. We have placed the maximum amount of blocks for this tick
        var i = 0.0
        while (i < blocksPerTick.value && i < blocks.size) {

            // Get position
            val pos = blocks.keys.toTypedArray()[i.toInt()]

            // Get facing
            val facing = blocks[pos]

            // Get rotation
            val rotation = getRotationToBlockPos(pos, 0.5)

            // Rotate to position
            if (rotate.value == Rotate.LEGIT) {
                minecraft.player.rotationYaw = rotation.x
                minecraft.player.rotationPitch = rotation.y
                minecraft.player.rotationYawHead = rotation.x
            }

            else if (rotate.value == Rotate.PACKET) {
                minecraft.player.connection.sendPacket(CPacketPlayer.Rotation(rotation.x, rotation.y, minecraft.player.onGround))
            }

            // Get current item
            val slot: Int = minecraft.player.inventory.currentItem

            // Slot to switch to
            val obsidianSlot = getHotbarBlockSlot(Blocks.OBSIDIAN)
            if (obsidianSlot != -1) {
                // Switch
                minecraft.player.inventory.currentItem = obsidianSlot

                // Make the server think we are crouching, so we can place on interactable blocks (e.g. chests, furnaces, etc.)
                minecraft.player.connection.sendPacket(CPacketEntityAction(minecraft.player, CPacketEntityAction.Action.START_SNEAKING))

                // Place block
                minecraft.playerController.processRightClickBlock(minecraft.player, minecraft.world, pos.offset(facing), facing!!.opposite, Vec3d(pos), EnumHand.MAIN_HAND)

                // Swing hand
                minecraft.player.swingArm(EnumHand.MAIN_HAND)

                // Stop crouching
                minecraft.player.connection.sendPacket(CPacketEntityAction(minecraft.player, CPacketEntityAction.Action.STOP_SNEAKING))
            }

            // Reset slot to our original slot
            minecraft.player.inventory.currentItem = slot
            i += 1.0
        }

        // Rotate back
        if (rotateBack.value!!) {
            if (rotate.value == Rotate.LEGIT) {
                minecraft.player.rotationYaw = originalRotation.x
                minecraft.player.rotationPitch = originalRotation.y
                minecraft.player.rotationYawHead = originalRotation.x
            }

            else if (rotate.value == Rotate.PACKET) {
                minecraft.player.connection.sendPacket(CPacketPlayer.Rotation(originalRotation.x, originalRotation.y, minecraft.player.onGround))
            }
        }

        // Toggle if we have placed all the blocks
        if (renderBlocks.isEmpty() && disable.value == Disable.FINISHED) {
            toggle()
        }
    }

    override fun onRender3D() {
        // We want to render
        if (render.value!!) {
            renderBlocks.forEach { (pos: BlockPos?, facing: EnumFacing?) ->
                RenderBuilder().boundingBox(getBlockBox(pos)).outer(renderColour.value!!.integrateAlpha(255f)).type(BoxRenderMode.BOTH).start().blend(true).depth(true).texture(true).lineWidth(1f).build(false)
            }
        }
    }

    private fun canPlaceBlock(pos: BlockPos): Boolean {
        // Iterate through entities in the block above
        for (entity in minecraft.world.getEntitiesWithinAABB(Entity::class.java, AxisAlignedBB(pos))) {
            // If the entity is dead, continue
            if (entity.isDead) {
                continue
            }
            return false
        }

        // Block is air
        return if (pos.getBlockAtPos() !== Blocks.AIR && !pos.getBlockAtPos().isReplaceable(minecraft.world, pos)) {
            false
        }

        else pos.down().getBlockAtPos() !== Blocks.AIR || pos.up().getBlockAtPos() !== Blocks.AIR || pos.north().getBlockAtPos() !== Blocks.AIR || pos.south().getBlockAtPos() !== Blocks.AIR || pos.east().getBlockAtPos() !== Blocks.AIR || pos.west().getBlockAtPos() !== Blocks.AIR

        // There is a block we can place on
    }

    @Listener
    fun onSettingChange(event: SettingUpdateEvent) {
        if (event.setting == threaded && threaded.value!!) {
            placingThread = Thread(Placer())
            placingThread!!.start()
        }
    }

    override fun onDisable() {
        placingThread = null
    }

    internal class Placer : Runnable {
        override fun run() {
            do {
                doSurround()
            } while (threaded.value!! && isEnabled)
        }
    }

    enum class Disable {
        /**
         * Disable once all blocks have been placed
         */
        FINISHED,

        /**
         * Never disable
         */
        NEVER,

        /**
         * Disable when the player is in air
         */
        AIR
    }

    enum class Center {
        /**
         * Move the player to the center
         */
        MOTION,

        /**
         * Snap the player to the center
         */
        SNAP,

        /**
         * Don't center the player
         */
        OFF
    }

    enum class Air {
        /**
         * Place blocks below air blocks, so we can place on top of them
         */
        SUPPORT,

        /**
         * Do not place on air blocks
         */
        IGNORE
    }

}