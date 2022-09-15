package com.paragon.impl.ui.console

import com.paragon.Paragon
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.ui.util.Click.Companion.getClick
import net.minecraft.client.gui.GuiScreen
import java.io.IOException

/**
 * @author Surge
 */
class ConsoleGUI : GuiScreen() {

    override fun initGui() {
        Paragon.INSTANCE.console.init()
        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        // Make the background darker
        if (ClickGUI.darkenBackground.value) {
            drawDefaultBackground()
        }

        Paragon.INSTANCE.console.draw(mouseX, mouseY)
        Paragon.INSTANCE.taskbar.draw(mouseX, mouseY)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        Paragon.INSTANCE.taskbar.mouseClicked(mouseX, mouseY, getClick(mouseButton))
        Paragon.INSTANCE.console.mouseClicked(mouseX, mouseY, mouseButton)

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        Paragon.INSTANCE.console.keyTyped(typedChar, keyCode)

        super.keyTyped(typedChar, keyCode)
    }

    override fun doesGuiPauseGame() = ClickGUI.pause.value

}