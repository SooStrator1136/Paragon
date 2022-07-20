package com.paragon.client.ui.configuration.window

import com.paragon.client.systems.module.impl.client.ClickGUI
import com.paragon.client.ui.configuration.window.window.Window
import com.paragon.client.ui.configuration.window.window.windows.ConfigurationWindow
import com.paragon.client.ui.util.Click
import net.minecraft.client.gui.GuiScreen

/**
 * @author Wolfsurge
 */
class WindowGUI : GuiScreen() {

    private val windows: ArrayList<Window> = ArrayList()

    init {
        windows.add(ConfigurationWindow("Paragon", 50f, 50f, 400f, 300f))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)

        windows.forEach {
            it.draw(mouseX, mouseY)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        windows.forEach {
            it.mouseClicked(mouseX, mouseY, Click.getClick(mouseButton))
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        windows.forEach {
            it.mouseReleased(mouseX, mouseY, Click.getClick(state))
        }

        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        windows.forEach {
            it.keyTyped(typedChar, keyCode)
        }

        super.keyTyped(typedChar, keyCode)
    }

    override fun doesGuiPauseGame() = ClickGUI.pause.value

}