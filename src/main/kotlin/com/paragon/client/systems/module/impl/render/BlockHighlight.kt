package com.paragon.client.systems.module.impl.render

import com.paragon.api.event.render.world.BlockHighlightEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.ColourUtil.integrateAlpha
import com.paragon.api.util.render.builder.BoxRenderMode
import com.paragon.api.util.render.builder.RenderBuilder
import com.paragon.bus.listener.Listener
import net.minecraft.util.math.RayTraceResult
import java.awt.Color

/**
 * @author Surge
 */
object BlockHighlight : Module("BlockHighlight", Category.RENDER, "Highlights the block you are looking at") {

    private val renderMode = Setting(
        "RenderMode",
        BoxRenderMode.BOTH
    ) describedBy "How to highlight the block"

    private val lineWidth = Setting(
        "LineWidth",
        1f,
        0.1f,
        1.5f,
        0.1f
    ) describedBy "The width of the outline" visibleWhen { renderMode.value != BoxRenderMode.FILL }

    private val colour = Setting(
        "Colour",
        Color(185, 19, 211)
    ) describedBy ("What colour to render the block")

    @Listener
    fun onBlockHighlight(event: BlockHighlightEvent) {
        event.cancel()
    }

    override fun onRender3D() {
        if (minecraft.objectMouseOver != null && minecraft.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
            val bp = minecraft.objectMouseOver.blockPos

            // Get bounding box (yoinked from RenderGlobal)
            val bb = minecraft.world.getBlockState(bp)
                .getSelectedBoundingBox(minecraft.world, bp)
                .grow(0.0020000000949949026)
                .offset(-minecraft.renderManager.viewerPosX, -minecraft.renderManager.viewerPosY, -minecraft.renderManager.viewerPosZ)

            /* RenderBuilder()
                .boundingBox(bb)
                .inner(colour.value)
                .outer(colour.value.integrateAlpha(255f))
                .type(renderMode.value)

                .start()

                .blend(true)
                .depth(true)
                .texture(true)
                .lineWidth(lineWidth.value)

                .build(true) */

            RenderBuilder()
                .boundingBox(bb)
                .inner(colour.value)
                .outer(colour.value.integrateAlpha(255f))
                .type(renderMode.value)

                .start()

                .blend(true)
                .depth(true)
                .texture(true)
                .lineWidth(lineWidth.value)

                .build(false)
        }
    }

}