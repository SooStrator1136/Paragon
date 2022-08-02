package com.paragon.client.systems.module.impl.render

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.Wrapper
import com.paragon.api.util.render.RenderUtil.drawBoundingBox
import com.paragon.api.util.render.RenderUtil.drawFilledBox
import com.paragon.api.util.render.RenderUtil.drawNametagText
import com.paragon.api.util.world.BlockUtil.getBlockAtPos
import com.paragon.api.util.world.BlockUtil.getBlockBox
import com.paragon.asm.mixins.accessor.IRenderGlobal
import net.minecraft.client.renderer.DestroyBlockProgress
import net.minecraft.init.Blocks
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import java.awt.Color

/**
 * @author Surge
 */
object BreakESP : Module("BreakESP", Category.RENDER, "Highlights blocks that are currently being broken") {

    // Render settings
    private val renderMode = Setting("RenderMode", RenderMode.BOTH)
        .setDescription("How to render the highlight")

    private val lineWidth = Setting("LineWidth", 1.0f, 0.1f, 3f, 0.1f)
        .setDescription("The width of the outline")
        .setVisibility { renderMode.value != RenderMode.FILL }

    // Other settings
    private val range = Setting("Range", 20f, 1f, 50f, 1f)
        .setDescription("The maximum distance a highlighted block can be")
    
    private val percent = Setting("Percent", true)
        .setDescription("Show the percentage of how much the block has been broken")

    override fun onRender3D() {
        // Iterate through all blocks being broken
        (minecraft.renderGlobal as IRenderGlobal).damagedBlocks.forEach { (pos: Int?, progress: DestroyBlockProgress?) ->
            if (progress != null) {
                // Get the block being broken
                val blockPos = progress.position

                // Don't care about air
                if (getBlockAtPos(blockPos) === Blocks.AIR) {
                    return@forEach
                }

                // Check block is within range
                if (blockPos.getDistance(minecraft.player.posX.toInt(), minecraft.player.posY.toInt(), minecraft.player.posZ.toInt()) <= range.value) {
                    // Block damage. Clamping this as it can go above 8 for other players, breaking the colour and throwing an exception
                    val damage = MathHelper.clamp(progress.partialBlockDamage, 0, 8)

                    // Block bounding box
                    val bb = getBlockBox(blockPos)

                    // Render values
                    val x = bb.minX + (bb.maxX - bb.minX) / 2
                    val y = bb.minY + (bb.maxY - bb.minY) / 2
                    val z = bb.minZ + (bb.maxZ - bb.minZ) / 2
                    val sizeX = damage * ((bb.maxX - x) / 8)
                    val sizeY = damage * ((bb.maxY - y) / 8)
                    val sizeZ = damage * ((bb.maxZ - z) / 8)

                    // The bounding box we will highlight
                    val highlightBB = AxisAlignedBB(x - sizeX, y - sizeY, z - sizeZ, x + sizeX, y + sizeY, z + sizeZ)

                    // The colour factor (for a transition between red and green (looks cool))
                    val colour = damage * 255 / 8
                    when (renderMode.value) {
                        RenderMode.FILL -> drawFilledBox(highlightBB, Color(255 - colour, colour, 0, 150))
                        
                        RenderMode.OUTLINE -> drawBoundingBox(
                            highlightBB,
                            lineWidth.value,
                            Color(255 - colour, colour, 0, 255)
                        )

                        RenderMode.BOTH -> {
                            drawFilledBox(highlightBB, Color(255 - colour, colour, 0, 150))
                            drawBoundingBox(highlightBB, lineWidth.value, Color(255 - colour, colour, 0, 255))
                        }

                        else -> {}
                    }

                    // Draw the percentage
                    if (percent.value) {
                        drawNametagText((damage * 100 / 8).toString() + "%", Vec3d((blockPos.x + 0.5f).toDouble(), (blockPos.y + 0.5f).toDouble(), (blockPos.z + 0.5f).toDouble()), -1)
                    }
                }
            }
        }
    }

    enum class RenderMode {
        /**
         * Fill the block
         */
        FILL,

        /**
         * Outline the block
         */
        OUTLINE,

        /**
         * Fill and outline the block
         */
        BOTH,

        /**
         * No render
         */
        NONE
    }
}