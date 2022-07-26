package com.paragon.client.ui.configuration.retrowindows.element

import com.paragon.api.util.render.ITextRenderer
import com.paragon.client.ui.util.Click

/**
 * @author Surge
 */
abstract class RawElement(var x: Float, var y: Float, var width: Float, var height: Float) : ITextRenderer {

    abstract fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int)
    open fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {}
    open fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {}
    open fun keyTyped(character: Char, keyCode: Int) {}

    fun isHovered(mouseX: Float, mouseY: Float): Boolean {
        return mouseX in x..x + width && mouseY in y..y + height
    }

}