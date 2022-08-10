package com.paragon.client.ui.configuration.shared


import com.paragon.client.ui.util.Click

/**
 * @author Surge
 */
abstract class RawElement(var x: Float, var y: Float, var width: Float, var height: Float) {

    abstract fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int)

    open fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {}
    open fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {}
    open fun keyTyped(character: Char, keyCode: Int) {}

    open fun isHovered(mouseX: Float, mouseY: Float) = mouseX in x..x + width && mouseY in y..y + height

}