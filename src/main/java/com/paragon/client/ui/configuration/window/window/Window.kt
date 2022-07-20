package com.paragon.client.ui.configuration.window.window

import com.paragon.api.util.render.ITextRenderer
import com.paragon.client.ui.util.Click

/**
 * @author Wolfsurge
 */
abstract class Window(var x: Float, var y: Float, var width: Float, var height: Float) : ITextRenderer {

    var scroll = 0f
    var scrollFactor = 0f

    abstract fun draw(mouseX: Int, mouseY: Int)
    abstract fun mouseClicked(mouseX: Int, mouseY: Int, button: Click)
    abstract fun mouseReleased(mouseX: Int, mouseY: Int, button: Click)
    abstract fun keyTyped(character: Char, keyCode: Int)

}