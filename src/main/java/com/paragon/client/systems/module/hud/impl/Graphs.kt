package com.paragon.client.systems.module.hud.impl

import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.calculations.MathsUtil
import com.paragon.api.util.render.ColourUtil
import com.paragon.api.util.render.ColourUtil.integrateAlpha
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.font.FontUtil
import com.paragon.client.systems.module.hud.HUDModule
import com.paragon.client.systems.module.impl.client.Colours
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.awt.geom.Rectangle2D
import kotlin.math.abs

/**
 * @author SooStrator1136
 */
object Graphs : HUDModule("Graphs", "") {

    private val scale = Setting(
        "Size",
        1.0,
        0.1,
        2.0,
        0.1
    ) describedBy "Size of the graphs"

    private val speed = Setting("Speed", true) describedBy "Displays a graph showing your speed"
    private val ping = Setting("Ping", true) describedBy "Displays a graph showing your ping"

    private val graphOffset = Setting(
        "Offset",
        5F,
        1F,
        20F,
        1F
    ) describedBy "Distance between the graphs"

    private val backgroundColor = Setting(
        "Background",
        Color.BLACK.integrateAlpha(100F)
    ) describedBy "Color of the background"
    private val borderColor = Setting("Border", Colours.mainColour.value) describedBy "Color of the surrounding"
    private val graphColor = Setting("Graph color", Colours.mainColour.value.darker()) describedBy "Color of the graph"

    private val graphs = arrayOf(
        Graph("Speed"),
        Graph("Ping")
    )

    override fun onEnable() {
        graphs[0] = Graph("Speed")
        graphs[1] = Graph("Ping")
    }

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        if (speed.value) {
            graphs[0].update(
                Speed.getPlayerSpeed(
                    minecraft.player.posX - minecraft.player.lastTickPosX,
                    minecraft.player.posZ - minecraft.player.lastTickPosZ
                )
            )
        }

        if (ping.value) {
            graphs[1].update(abs(Ping.getPing()).toDouble())
        }
    }

    override fun render() {
        graphs[0].isVisible = speed.value
        graphs[1].isVisible = ping.value

        var i = 0
        graphs.forEach {
            if (it.isVisible) {
                it.bounds.setRect(
                    x,
                    y + (30F * i) + (graphOffset.value * i),
                    75F,
                    30F
                )

                RenderUtil.scaleTo(x, y, 1F, scale.value, scale.value, 1.0) {
                    it.render()
                }

                i++
            }
        }
    }

    internal class Graph(private val name: String) {

        var isVisible = true
        val bounds = Rectangle2D.Float()

        private var highestVal = 0.1

        private val points = Array(75) { 0.0 }

        private val graphRect = Rectangle2D.Float()

        fun render() {
            graphRect.setRect(bounds.x, bounds.y, bounds.width, bounds.height - FontUtil.getHeight() - 3F)

            //Basic background & border
            run {
                RenderUtil.drawRect(
                    bounds.x,
                    bounds.y,
                    bounds.width,
                    bounds.height,
                    backgroundColor.value.rgb
                )
                RenderUtil.drawBorder(
                    bounds.x,
                    bounds.y,
                    bounds.width,
                    bounds.height,
                    1F,
                    borderColor.value.rgb
                )

                FontUtil.drawStringWithShadow(
                    name,
                    x + 1F,
                    bounds.y + bounds.height - (FontUtil.getHeight() + 1F),
                    -1
                )

                RenderUtil.drawRect(
                    x,
                    bounds.y + bounds.height - (FontUtil.getHeight() + 3F),
                    bounds.width,
                    1F,
                    borderColor.value.rgb
                )
            }

            run {
                GlStateManager.alphaFunc(GL_GREATER, 0.001f)
                GlStateManager.enableAlpha()
                GlStateManager.enableBlend()
                GlStateManager.disableTexture2D()
                GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0)
                glLineWidth(1f)

                glBegin(GL_LINE_STRIP)
                ColourUtil.setColour(graphColor.value.rgb)
                points.forEachIndexed { i, percentage ->
                    glVertex2f(
                        graphRect.x + i,
                        (graphRect.y + (graphRect.height - MathsUtil.getPercentOf(
                            percentage,
                            graphRect.height.toDouble()
                        ))).toFloat()
                    )
                }
                glEnd()

                GlStateManager.alphaFunc(GL_GREATER, 0.1f)
                GlStateManager.color(1f, 1f, 1f, 1f)
                GlStateManager.disableBlend()
                GlStateManager.enableTexture2D()
            }
        }

        fun update(value: Double) {
            for (i in points.indices) { //Shifting all values -> Making space for the new
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

    }

    override var width = 75F

    override var height = 30F
        get() = if (speed.value && ping.value) 60F + graphOffset.value else 30F

}