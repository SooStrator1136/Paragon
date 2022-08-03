package com.paragon.client.systems.module.impl.render

import com.paragon.api.event.render.world.BlockHighlightEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.ColourUtil.integrateAlpha
import com.paragon.api.util.render.RenderUtil.drawBoundingBox
import com.paragon.api.util.render.RenderUtil.drawFilledBox
import com.paragon.api.util.world.BlockUtil.getBlockBox
import me.wolfsurge.cerauno.listener.Listener
import net.minecraft.util.math.RayTraceResult
import java.awt.Color

/**
 * @author Surge
 */
object BlockHighlight : Module("BlockHighlight", Category.RENDER, "Highlights the block you are looking at") {

    private val renderMode = Setting("RenderMode", RenderMode.BOTH)
        .setDescription("How to highlight the block")

    private val lineWidth = Setting("LineWidth", 1f, 0.1f, 1.5f, 0.1f)
        .setDescription("The width of the outline")
        .setVisibility { renderMode.value != RenderMode.FILL }

    private val colour = Setting("Colour", Color(185, 19, 211))
        .setDescription("What colour to render the block")

    @Listener
    fun onBlockHighlight(event: BlockHighlightEvent) {
        event.cancel()
    }

    override fun onRender3D() {
        if (minecraft.objectMouseOver != null && minecraft.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
            // Get bounding box
            val bb = getBlockBox(minecraft.objectMouseOver.blockPos)

            // Draw fill
            if (renderMode.value != RenderMode.OUTLINE) {
                drawFilledBox(bb, integrateAlpha(colour.value, 180f))
            }

            // Draw outline
            if (renderMode.value != RenderMode.FILL) {
                drawBoundingBox(bb, lineWidth.value, colour.value)
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
        BOTH
    }

}