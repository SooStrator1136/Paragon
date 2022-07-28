package com.paragon.client.ui.configuration

import net.minecraft.client.Minecraft

/**
 * @author Surge
 * @since 27/07/2022
 */
abstract class GuiImplementation {

    var width = 0f
    var height = 0f

    open fun initGui() {}

    abstract fun drawScreen(mouseX: Int, mouseY: Int, mouseDelta: Int)
    abstract fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int)
    abstract fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int)
    abstract fun keyTyped(typedChar: Char, keyCode: Int)

    abstract fun onGuiClosed()
    abstract fun doesGuiPauseGame(): Boolean

    fun drawDefaultBackground() {
        Minecraft.getMinecraft().currentScreen?.drawDefaultBackground()
    }

}