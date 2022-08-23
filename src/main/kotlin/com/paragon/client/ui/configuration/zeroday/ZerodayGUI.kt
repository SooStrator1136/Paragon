package com.paragon.client.ui.configuration.zeroday

import com.paragon.Paragon
import com.paragon.api.module.Category
import com.paragon.api.util.render.RenderUtil.screenWidth
import com.paragon.client.systems.module.impl.client.ClickGUI.easing
import com.paragon.client.systems.module.impl.client.ClickGUI.gradientBackground
import com.paragon.client.systems.module.impl.client.ClickGUI.pause
import com.paragon.client.ui.configuration.GuiImplementation
import com.paragon.client.ui.configuration.zeroday.panel.CategoryPanel
import com.paragon.client.ui.configuration.zeroday.panel.Panel
import com.paragon.client.ui.util.Click.Companion.getClick
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11.*
import java.util.function.Consumer

class ZerodayGUI : GuiImplementation() {

    private val panels: MutableList<Panel?> = ArrayList()

    val animation = Animation({ // Linear is apparently slower than other easings, so we decrease the delay
        if (easing.value == Easing.LINEAR) 200f else 500f
    }, false, easing::value)

    init {
        var x = screenWidth / 2 - Category.values().size * 110 / 2f
        for (category in Category.values()) {
            panels.add(CategoryPanel(category, x, 30f, 105f, 22f, 22f))
            x += 110f
        }
    }

    override fun initGui() {
        animation.state = true
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, mouseDelta: Int) {
        animation.state = true

        if (gradientBackground.value) {
            val topLeft = floatArrayOf(182f, 66f, 245f)
            val topRight = floatArrayOf(236f, 66f, 245f)
            val bottomRight = floatArrayOf(245f, 66f, 141f)
            val bottomLeft = floatArrayOf(212f, 11f, 57f)

            GlStateManager.pushMatrix()
            GlStateManager.disableTexture2D()
            GlStateManager.enableBlend()
            GlStateManager.disableAlpha()
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
            GlStateManager.shadeModel(7425)

            val tessellator = Tessellator.getInstance()
            val bufferbuilder = tessellator.buffer
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)
            bufferbuilder.pos(width.toDouble(), 0.0, 0.0).color(topRight[0] / 360, topRight[1] / 360, topRight[2] / 360, (0.5f * animation.getAnimationFactor()).toFloat()).endVertex()
            bufferbuilder.pos(0.0, 0.0, 0.0).color(topLeft[0] / 360, topLeft[1] / 360, topLeft[2] / 360, (0.5f * animation.getAnimationFactor()).toFloat()).endVertex()
            bufferbuilder.pos(0.0, height.toDouble(), 0.0).color(bottomLeft[0] / 360, bottomLeft[1] / 360, bottomLeft[2] / 360, (0.5f * animation.getAnimationFactor()).toFloat()).endVertex()
            bufferbuilder.pos(width.toDouble(), height.toDouble(), 0.0).color(bottomRight[0] / 360, bottomRight[1] / 360, bottomRight[2] / 360, (0.5f * animation.getAnimationFactor()).toFloat()).endVertex()
            tessellator.draw()

            GlStateManager.shadeModel(7424)
            GlStateManager.enableAlpha()
            GlStateManager.enableTexture2D()
            GlStateManager.popMatrix()
        }

        glPushMatrix()

        // pop out
        glScaled(animation.getAnimationFactor(), animation.getAnimationFactor(), 1.0)
        glTranslated(width / 2f * (1 - animation.getAnimationFactor()), height / 2f * (1 - animation.getAnimationFactor()), 0.0)

        panels.reverse()

        // grr lambdas
        val tooltip = arrayOf("")
        panels.forEach(Consumer { panel: Panel? ->
            panel!!.render(mouseX, mouseY, mouseDelta)
            if (panel is CategoryPanel && tooltip[0].isEmpty()) {
                if (panel.tooltip !== "") {
                    tooltip[0] = panel.tooltip
                }
            }
        })

        panels.reverse()

        glColor4f(1f, 1f, 1f, 1f)
        glPopMatrix()
        glPushMatrix()
        glTranslated(0.0, 24 - 24 * animation.getAnimationFactor(), 0.0)

        Paragon.INSTANCE.taskbar.tooltip = tooltip[0]

        glPopMatrix()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        panels.reverse()
        panels.forEach(Consumer { panel: Panel? -> panel!!.mouseClicked(mouseX, mouseY, getClick(mouseButton)) })
        panels.reverse()
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        panels.reverse()
        panels.forEach(Consumer { panel: Panel? -> panel!!.mouseReleased(mouseX, mouseY, getClick(state)) })
        panels.reverse()
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        panels.reverse()
        panels.forEach(Consumer { panel: Panel? -> panel!!.keyTyped(keyCode, typedChar) })
        panels.reverse()
    }

    override fun onGuiClosed() {
        Paragon.INSTANCE.storageManager.saveModules("current")
        Paragon.INSTANCE.storageManager.saveOther()
        animation.resetToDefault()
    }

    override fun doesGuiPauseGame(): Boolean {
        // Pause the game if pause is enabled in the GUI settings
        return pause.value
    }
}