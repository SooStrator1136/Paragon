package com.paragon.client.systems.module.impl.render

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.ColourUtil
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.world.BlockUtil
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import java.awt.Color
import java.util.function.Consumer

/**
 * @author Wolfsurge
 */
object VoidinqESP : Module("VoidinqESP", Category.RENDER, "Highlights void holes in the world") {

    private val range = Setting("Range", 5f, 0f, 20f, 1f)
        .setDescription("The range to check for holes")

    // Render settings
    private var fill = Setting<Boolean?>("Fill", true)
        .setDescription("Fill the holes ;)")

    private var fillHeight = Setting("Height", 0f, 0f, 2f, 0.01f)
        .setDescription("How tall the fill is")
        .setParentSetting(fill)

    private var outline = Setting<Boolean?>("Outline", true)
        .setDescription("Outline the hole")

    private var outlineWidth = Setting("Width", 1f, 1f, 3f, 1f)
        .setDescription("The width of the outlines")
        .setParentSetting(outline)

    private var outlineHeight = Setting("Height", 0f, 0f, 2f, 0.01f)
        .setDescription("How tall the outline is")
        .setParentSetting(outline)

    private var glow = Setting<Boolean?>("Gradient", true)
        .setDescription("Renders a glow effect above the box")

    private var glowHeight = Setting("Height", 1f, 0f, 2f, 0.01f)
        .setDescription("How tall the glow is")
        .setParentSetting(glow)

    private val colour = Setting("Colour", Color(200, 0, 0, 150))
        .setDescription("The highlight colour")

    private val holes: ArrayList<BlockPos> = ArrayList()

    override fun onTick() {
        if (nullCheck()) {
            return
        }

        holes.clear()

        BlockUtil.getSphere(range.value, false).forEach {
            if (it.y == 0 && minecraft.world.getBlockState(it).material.isReplaceable) {
                holes.add(it)
            }
        }
    }

    override fun onRender3D() {
        holes.forEach(Consumer {
            val blockBB = BlockUtil.getBlockBox(it)
            if (fill.value!!) {
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

            if (outline.value!!) {
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

            if (glow.value!!) {
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
        })
    }

}