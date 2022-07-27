package com.paragon.client.ui.configuration

import com.paragon.client.systems.module.impl.client.ClickGUI
import com.paragon.client.ui.configuration.windows.Window
import com.paragon.client.ui.util.Click
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Mouse

/**
 * @author Surge
 * @since 27/07/2022
 */
class ConfigurationGUI : GuiScreen() {

    var currentGUI: GuiImplementation? = null
    val windowsList: MutableList<Window> = mutableListOf()
    val removeBuffer: MutableList<Window> = mutableListOf()

    init {
        currentGUI = ClickGUI.getGUI()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)

        if (currentGUI != ClickGUI.getGUI()) {
            currentGUI = ClickGUI.getGUI()
            currentGUI!!.initGui()
        }

        val mouseDelta = Mouse.getDWheel()

        if (removeBuffer.isNotEmpty()) {
            windowsList.removeIf(removeBuffer::contains)
        }

        if (currentGUI != null) {
            currentGUI!!.drawScreen(mouseX, mouseY, mouseDelta)
        }

        windowsList.forEach { it.draw(mouseX, mouseY, mouseDelta) }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)

        windowsList.forEach {
            if (it.mouseClicked(mouseX, mouseY, Click.getClick(mouseButton))) {
                return
            }
        }

        if (currentGUI != null) {
            currentGUI!!.mouseClicked(mouseX, mouseY, mouseButton)
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        super.mouseReleased(mouseX, mouseY, state)

        windowsList.forEach { it.mouseReleased(mouseX, mouseY, Click.getClick(state)) }

        if (currentGUI != null) {
            currentGUI!!.mouseReleased(mouseX, mouseY, state)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)

        if (currentGUI != null) {
            currentGUI!!.keyTyped(typedChar, keyCode)
        }

        windowsList.forEach { it.keyTyped(typedChar, keyCode) }
    }

    override fun onGuiClosed() {
        super.onGuiClosed()

        if (currentGUI != null) {
            currentGUI!!.onGuiClosed()
        }
    }

    override fun doesGuiPauseGame(): Boolean {
        return ClickGUI.pause.value
    }

}