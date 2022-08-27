package com.paragon.client.systems.module.impl.render

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.RenderUtil.drawNametagText
import com.paragon.api.util.render.builder.BoxRenderMode
import com.paragon.api.util.render.builder.RenderBuilder
import com.paragon.api.util.world.BlockUtil.getBlockAtPos
import com.paragon.api.util.world.BlockUtil.getBlockBox
import com.paragon.mixins.accessor.IRenderGlobal
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
    private val renderMode = Setting(
        "RenderMode",
        BoxRenderMode.BOTH
    ) describedBy "How to render the highlight"

    private val lineWidth = Setting(
        "LineWidth",
        1.0f,
        0.1f,
        3f,
        0.1f
    ) describedBy "The width of the outline" visibleWhen { renderMode.value != BoxRenderMode.FILL }

    // Other settings
    private val range = Setting(
        "Range",
        20f,
        1f,
        50f,
        1f
    ) describedBy "The maximum distance a highlighted block can be"

    private val percent = Setting(
        "Percent",
        true
    ) describedBy "Show the percentage of how much the block has been broken"

    override fun onRender3D() {
        // Iterate through all blocks being broken
        (minecraft.renderGlobal as IRenderGlobal).damagedBlocks!!.forEach { (_: Int?, progress: DestroyBlockProgress?) ->
            if (progress == null) {
                return@forEach
            }

            // Get the block being broken
            val blockPos = progress.position

            // Don't care about air
            if (blockPos.getBlockAtPos() === Blocks.AIR) {
                return@forEach
            }

            // Check block is within range
            if (blockPos.getDistance(
                    minecraft.player.posX.toInt(),
                    minecraft.player.posY.toInt(),
                    minecraft.player.posZ.toInt()
                ) > range.value
            ) {
                return@forEach
            }

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

            RenderBuilder()
                .boundingBox(highlightBB)
                .inner(Color(255 - colour, colour, 0, 150))
                .outer(Color(255 - colour, colour, 0, 255))
                .type(renderMode.value)

                .start()

                .blend(true)
                .depth(true)
                .texture(true)
                .lineWidth(lineWidth.value)

                .build(false)

            // Draw the percentage
            if (percent.value) {
                drawNametagText(
                    (damage * 100 / 8).toString() + "%",
                    Vec3d(
                        (blockPos.x + 0.5f).toDouble(),
                        (blockPos.y + 0.5f).toDouble(),
                        (blockPos.z + 0.5f).toDouble()
                    ),
                    -1
                )
            }
        }
    }

}