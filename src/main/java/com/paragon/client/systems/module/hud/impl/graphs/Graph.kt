package com.paragon.client.systems.module.hud.impl.graphs

import com.paragon.api.util.calculations.MathsUtil
import com.paragon.api.util.render.ColourUtil
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.font.FontUtil
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.awt.geom.Rectangle2D

/**
 * @author SooStrator1136
 */
class Graph(
    private val name: String,
    private val backgroundColor: () -> Color,
    private val borderColor: () -> Color,
    private val graphColor: () -> Color,
    private val backgroundMode: () -> Background
) {

    private val graphRect = Rectangle2D.Float()
    val bounds = Rectangle2D.Float()

    private var highestVal = 0.1

    private val points = Array(75) { 0.0 }

    fun render() {
        graphRect.setRect(bounds.x, bounds.y, bounds.width, bounds.height - FontUtil.getHeight() - 3F)

        val background = backgroundMode()

        //Basic background & border
        run {
            if (background != Background.NONE) {
                RenderUtil.drawRect(
                    bounds.x,
                    bounds.y,
                    bounds.width,
                    bounds.height,
                    backgroundColor().rgb
                )
            }
            if (background == Background.ALL) {
                RenderUtil.drawBorder(
                    bounds.x,
                    bounds.y,
                    bounds.width,
                    bounds.height,
                    1F,
                    borderColor().rgb
                )
            }

            FontUtil.drawStringWithShadow(
                name,
                bounds.x + 1F,
                bounds.y + bounds.height - (FontUtil.getHeight() + 1F),
                -1
            )

            if (background == Background.ALL) {
                RenderUtil.drawRect(
                    bounds.x,
                    bounds.y + bounds.height - (FontUtil.getHeight() + 3F),
                    bounds.width,
                    1F,
                    borderColor().rgb
                )
            }
        }

        run {
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.001f)
            GlStateManager.enableAlpha()
            GlStateManager.enableBlend()
            GlStateManager.disableTexture2D()
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
            GL11.glLineWidth(1f)

            GL11.glBegin(GL11.GL_LINE_STRIP)
            ColourUtil.setColour(graphColor().rgb)
            points.forEachIndexed { i, percentage ->
                GL11.glVertex2f(
                    graphRect.x + i,
                    (graphRect.y + (graphRect.height - MathsUtil.getPercentOf(
                        percentage,
                        graphRect.height.toDouble()
                    ))).toFloat()
                )
            }
            GL11.glEnd()

            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f)
            GlStateManager.color(1f, 1f, 1f, 1f)
            GlStateManager.disableBlend()
            GlStateManager.enableTexture2D()
        }
    }

    fun update(value: Double) {
        for (i in points.indices) { //Shifting all values -> Making space for the new val
            if (i != points.size - 1) {
                points[i] = points[i + 1]
            }
        }

        if (value > highestVal) { //Changing all the already given values according to the new highest value
            val oldMax = highestVal
            highestVal = value

            for (i in points.indices) {
                if (i != points.size - 1) {
                    points[i] = MathsUtil.getPercent(MathsUtil.getPercentOf(points[i], oldMax), highestVal)
                }
            }
        }

        points[points.size - 1] = MathsUtil.getPercent(value, highestVal)
    }

    enum class Background {
        NONE, ALL, SIMPLE
    }

}