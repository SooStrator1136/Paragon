package com.paragon.impl.ui.configuration

import com.paragon.Paragon
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.module.client.ClickGUI.darkenBackground
import com.paragon.impl.ui.util.Click
import com.paragon.impl.ui.windows.Window
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Mouse

/**
 * @author Surge
 * @since 27/07/2022
 */
class ConfigurationGUI : GuiScreen() {

    var closeOnEscape = true

    private var currentGUI: GuiImplementation? = null
    val windowsList: MutableList<Window> = mutableListOf()
    val removeBuffer: MutableList<Window> = mutableListOf()

    init {
        currentGUI = ClickGUI.getGUI()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)

        Paragon.INSTANCE.taskbar.tooltip = ""

        if (currentGUI != ClickGUI.getGUI()) {
            currentGUI = ClickGUI.getGUI()
            currentGUI?.initGui()
        }

        var mouseDelta = Mouse.getDWheel()

        if (removeBuffer.isNotEmpty()) {
            windowsList.removeIf(removeBuffer::contains)
        }

        windowsList.forEach {
            if (it.scroll(mouseX, mouseY, mouseDelta)) {
                mouseDelta = 0
            }
        }

        if (darkenBackground.value) {
            drawDefaultBackground()
        }

        currentGUI?.width = width.toFloat()
        currentGUI?.height = height.toFloat()
        currentGUI?.drawScreen(mouseX, mouseY, mouseDelta)

        windowsList.forEach { it.draw(mouseX, mouseY, mouseDelta) }

        Paragon.INSTANCE.taskbar.draw(mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)

        windowsList.reverse()

        windowsList.forEach {
            if (it.mouseClicked(mouseX, mouseY, Click.getClick(mouseButton))) {
                return
            }
        }

        windowsList.reverse()

        currentGUI?.mouseClicked(mouseX, mouseY, mouseButton)

        Paragon.INSTANCE.taskbar.mouseClicked(mouseX, mouseY, Click.getClick(mouseButton))
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        super.mouseReleased(mouseX, mouseY, state)

        windowsList.forEach { it.mouseReleased(mouseX, mouseY, Click.getClick(state)) }

        currentGUI?.mouseReleased(mouseX, mouseY, state)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (closeOnEscape) {
            super.keyTyped(typedChar, keyCode)
        }

        currentGUI?.keyTyped(typedChar, keyCode)

        windowsList.forEach { it.keyTyped(typedChar, keyCode) }
    }

    override fun onGuiClosed() {
        super.onGuiClosed()

        currentGUI?.onGuiClosed()
    }

    override fun doesGuiPauseGame() = ClickGUI.pause.value

}