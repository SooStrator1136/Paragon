package com.paragon.client.ui.configuration.old

import com.paragon.Paragon
import com.paragon.api.module.Category
import com.paragon.api.util.render.RenderUtil.drawBorder
import com.paragon.api.util.render.RenderUtil.drawRect
import com.paragon.api.util.render.RenderUtil.screenWidth
import com.paragon.api.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.api.util.render.font.FontUtil.getHeight
import com.paragon.api.util.render.font.FontUtil.getStringWidth
import com.paragon.client.systems.module.impl.client.ClickGUI.darkenBackground
import com.paragon.client.systems.module.impl.client.ClickGUI.pause
import com.paragon.client.systems.module.impl.client.ClickGUI.scrollSpeed
import com.paragon.client.systems.module.impl.client.ClickGUI.tooltips
import com.paragon.client.systems.module.impl.client.ClientFont
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.GuiImplementation
import com.paragon.client.ui.configuration.old.impl.Panel
import org.lwjgl.input.Mouse
import java.util.function.Consumer

/**
 * @author Wolfsurge
 */
class OldPanelGUI private constructor() : GuiImplementation() {

    // List of panels
    val panels = ArrayList<Panel>()

    init {
        // X position of panel
        var x = screenWidth / 2 - Category.values().size * 100 / 2f

        // Add a panel for every category
        for (category in Category.values()) {
            // Add panel
            panels.add(Panel(x, 30f, 95f, 16f, category))

            // Increase X
            x += 100f
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, mouseDelta: Int) {
        // Reset tooltip
        tooltip = ""

        for (panel in panels) {
            panel.y = panel.y + mouseDelta / 100f * scrollSpeed.value
        }

        // Render panels
        panels.forEach(Consumer { panel: Panel -> panel.renderPanel(mouseX, mouseY) })
        if (tooltips.value && tooltip != "") {
            drawRect((mouseX + 7).toFloat(), (mouseY - 5).toFloat(), getStringWidth(tooltip) + 4, getHeight() + 2, -0x70000000)
            drawBorder((mouseX + 7).toFloat(), (mouseY - 5).toFloat(), getStringWidth(tooltip) + 4, getHeight() + 2, 0.5f, Colours.mainColour.value.rgb)
            drawStringWithShadow(tooltip, (mouseX + 9).toFloat(), (mouseY - if (ClientFont.isEnabled) 2 else 4).toFloat(), -1)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        // Clicks
        panels.forEach(Consumer { panel: Panel -> panel.mouseClicked(mouseX, mouseY, mouseButton) })
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        // Click releases
        panels.forEach(Consumer { panel: Panel -> panel.mouseReleased(mouseX, mouseY, state) })
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        // Keys typed
        panels.forEach(Consumer { panel: Panel -> panel.keyTyped(typedChar, keyCode) })
    }

    override fun onGuiClosed() {
        Paragon.INSTANCE.storageManager.saveModules("current")
    }

    fun scrollPanels() {
        val dWheel = Mouse.getDWheel()
        for (panel in panels) {
            panel.y = panel.y + dWheel / 100f * scrollSpeed.value
        }
    }

    override fun doesGuiPauseGame(): Boolean {
        // Pause the game if pause is enabled in the GUI settings
        return pause.value
    }

    companion object {
        var INSTANCE = OldPanelGUI()

        // The tooltip being rendered
        @JvmField
        var tooltip = ""
        @JvmStatic
        fun isInside(x: Float, y: Float, x2: Float, y2: Float, checkX: Int, checkY: Int): Boolean {
            return checkX >= x && checkX <= x2 && checkY > y && checkY <= y2
        }
    }
}