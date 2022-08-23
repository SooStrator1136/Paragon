package com.paragon.client.systems.module.hud

import com.paragon.Paragon
import com.paragon.api.util.render.RenderUtil.drawRect
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color
import java.io.IOException

/**
 * @author SooStrator1136
 */
class HUDEditorGUI : GuiScreen() {

    private var draggingComponent = false

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()

        val scaledResolution = ScaledResolution(mc)

        drawRect(
            scaledResolution.scaledWidth / 2f - 0.5f,
            0f,
            1f,
            scaledResolution.scaledHeight.toFloat(),
            Color(255, 255, 255, 100).rgb
        )
        drawRect(
            0f,
            scaledResolution.scaledHeight / 2f - 0.5f,
            scaledResolution.scaledWidth.toFloat(),
            1f,
            Color(255, 255, 255, 100).rgb
        )

        Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it is HUDModule && it.isEnabled }.forEach {
            (it as HUDModule).updateComponent(mouseX, mouseY)
            it.render()
        }

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

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        draggingComponent = false
        Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it is HUDModule && it.isEnabled }.forEach {
            (it as HUDModule).mouseReleased(mouseX, mouseY, state)
        }

        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun onGuiClosed() {
        draggingComponent = false
        Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it is HUDModule && it.isEnabled }.forEach {
            (it as HUDModule).mouseReleased(0, 0, 0)
        }
    }

}