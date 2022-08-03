package com.paragon.client.systems.module.impl.render

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.render.ColourUtil
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.world.BlockUtil
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import java.awt.Color

/**
 * @author Surge
 */
object VoidinqESP : Module("VoidinqESP", Category.RENDER, "Highlights void holes in the world") {

    private val range = Setting("Range", 5f, 0f, 20f, 1f)
        .setDescription("The range to check for holes")

    // Render settings
    private val fill = Setting("Fill", true)
        .setDescription("Fill the holes ;)")

    private val fillHeight = Setting("Height", 0f, 0f, 2f, 0.01f)
        .setDescription("How tall the fill is")
        .setParentSetting(fill)

    private val outline = Setting("Outline", true)
        .setDescription("Outline the hole")

    private val outlineWidth = Setting("Width", 1f, 1f, 3f, 1f)
        .setDescription("The width of the outlines")
        .setParentSetting(outline)

    private val outlineHeight = Setting("Height", 0f, 0f, 2f, 0.01f)
        .setDescription("How tall the outline is")
        .setParentSetting(outline)

    private val glow = Setting("Gradient", true)
        .setDescription("Renders a glow effect above the box")

    private val glowHeight = Setting("Height", 1f, 0f, 2f, 0.01f)
        .setDescription("How tall the glow is")
        .setParentSetting(glow)

    private val colour = Setting("Colour", Color(200, 0, 0, 150))
        .setDescription("The highlight colour")

    private val holes: ArrayList<BlockPos> = ArrayList()

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        holes.clear()

        holes.addAll(BlockUtil.getSphere(range.value, false).filter {
            it.y == 0 && minecraft.world.getBlockState(it).material.isReplaceable
        })
    }

    override fun onRender3D() {
        holes.forEach {
            val blockBB = BlockUtil.getBlockBox(it)
            if (fill.value) {
                val fillBB = AxisAlignedBB(
                    blockBB.minX,
                    blockBB.minY,
                    blockBB.minZ,
                    blockBB.maxX,
                    blockBB.minY + fillHeight.value,
                    blockBB.maxZ
                )
                RenderUtil.drawFilledBox(fillBB, colour.value)
            }

            if (outline.value) {
                val outlineBB = AxisAlignedBB(
                    blockBB.minX,
                    blockBB.minY,
                    blockBB.minZ,
                    blockBB.maxX,
                    blockBB.minY + outlineHeight.value,
                    blockBB.maxZ
                )
                RenderUtil.drawBoundingBox(
                    outlineBB,
                    outlineWidth.value,
                    ColourUtil.integrateAlpha(colour.value, 255f)
                )
            }

            if (glow.value) {
                val glowBB = AxisAlignedBB(
                    blockBB.minX,
                    blockBB.minY,
                    blockBB.minZ,
                    blockBB.maxX,
                    blockBB.minY + glowHeight.value,
                    blockBB.maxZ
                )
                RenderUtil.drawGradientBox(glowBB, Color(0, 0, 0, 0), colour.value)
            }
        }
    }

}