package com.paragon.client.ui.configuration.windows

import com.paragon.Paragon
import com.paragon.api.util.render.ITextRenderer
import com.paragon.client.ui.util.Click
import com.paragon.client.ui.util.animation.Animation
import com.paragon.client.ui.util.animation.Easing
import net.minecraft.util.math.Vec2f

/**
 * @author Surge
 * @since 27/07/2022
 */
abstract class Window(var x: Float, var y: Float, var width: Float, var height: Float, var grabbableHeight: Float) : ITextRenderer {

    var lastPosition: Vec2f = Vec2f(0f, 0f)
    var dragging = false

    val openAnimation = Animation({ 500f }, false, { Easing.EXPO_IN_OUT })

    init {
        openAnimation.state = true
    }

    open fun draw(mouseX: Int, mouseY: Int, mouseDelta: Int) {
        if (dragging) {
            x = mouseX - lastPosition.x
            y = mouseY - lastPosition.y
        }

        if (openAnimation.getAnimationFactor() == 0.0 && !openAnimation.state) {
            Paragon.INSTANCE.configurationGUI.removeBuffer.add(this)
        }
    }

    open fun mouseClicked(mouseX: Int, mouseY: Int, click: Click): Boolean {
        if (click == Click.LEFT && mouseX.toFloat() in x..x + width && mouseY.toFloat() in y..y + grabbableHeight) {
            dragging = true
            lastPosition = Vec2f(mouseX - x, mouseY - y)

            return true
        }

        return false
    }

    open fun mouseReleased(mouseX: Int, mouseY: Int, click: Click) {
        dragging = false
    }

    open fun keyTyped(character: Char, keyCode: Int) {}

}