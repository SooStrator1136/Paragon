package com.paragon.impl.module.hud

import com.paragon.Paragon
import com.paragon.impl.module.Category
import com.paragon.impl.ui.configuration.panel.impl.CategoryPanel
import com.paragon.impl.ui.util.Click
import com.paragon.util.render.BlurUtil
import com.paragon.util.render.RenderUtil
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Mouse
import java.awt.Color
import java.io.IOException

/**
 * @author SooStrator1136
 */
class HUDEditorGUI : GuiScreen() {

    private var draggingComponent = false
    private val panel = CategoryPanel(null, Category.HUD, 200f, 20f, 80f, 22f, 200.0)

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val scaledResolution = ScaledResolution(mc)

        RenderUtil.drawRect(0f, 0f, scaledResolution.scaledWidth.toFloat(), scaledResolution.scaledHeight.toFloat(), Color(0, 0, 0, 180))
        BlurUtil.blur(0f, 0f, scaledResolution.scaledWidth.toFloat(), scaledResolution.scaledHeight.toFloat(), 5f)

        RenderUtil.drawRect(scaledResolution.scaledWidth / 2f - 0.5f, 0f, 1f, scaledResolution.scaledHeight.toFloat(), Color(255, 255, 255, 100))
        RenderUtil.drawRect(0f, scaledResolution.scaledHeight / 2f - 0.5f, scaledResolution.scaledWidth.toFloat(), 1f, Color(255, 255, 255, 100))

        Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it is HUDModule && it.animation.getAnimationFactor() > 0 }.forEach {
            (it as HUDModule).updateComponent(mouseX, mouseY)

            RenderUtil.scaleTo(it.x + (it.width / 2), it.y + (it.height / 2), 0f, it.animation.getAnimationFactor(), it.animation.getAnimationFactor(), 0.0) {
                it.render()
            }
        }

        panel.draw(mouseX.toFloat(), mouseY.toFloat(), Mouse.getDWheel())

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        Paragon.INSTANCE.moduleManager.modules.reverse()

        run {
            Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it is HUDModule && it.isEnabled }.forEach {
                if (!draggingComponent) {
                    if ((it as HUDModule).mouseClicked(mouseX, mouseY, mouseButton)) {
                        return@run
                    }

                    if (it.isDragging) {
                        draggingComponent = true
                    }
                }
            }
        }

        Paragon.INSTANCE.moduleManager.modules.reverse()

        if (!draggingComponent) {
            panel.mouseClicked(mouseX.toFloat(), mouseY.toFloat(), Click.getClick(mouseButton))
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        draggingComponent = false
        Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it is HUDModule && it.isEnabled }.forEach {
            (it as HUDModule).mouseReleased(mouseX, mouseY, state)
        }

        panel.mouseReleased(mouseX.toFloat(), mouseY.toFloat(), Click.getClick(state))

        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun onGuiClosed() {
        draggingComponent = false

        Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it is HUDModule && it.isEnabled }.forEach {
            (it as HUDModule).mouseReleased(0, 0, 0)
        }
    }

}