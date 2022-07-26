package com.paragon.client.ui.configuration.retrowindows.window

import com.paragon.client.ui.configuration.retrowindows.element.RawElement
import com.paragon.client.ui.util.Click
import net.minecraft.util.math.Vec2f

/**
 * @author Surge
 */
abstract class Window(x: Float, y: Float, width: Float, height: Float) : RawElement(x, y, width, height) {

    lateinit var title: String
    var dragging = false

    var lastPosition: Vec2f = Vec2f(0f, 0f)

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        if (dragging) {
            x = mouseX - lastPosition.x
            y = mouseY - lastPosition.y
        }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (click == Click.LEFT) {
            if (mouseX in x..x + width && mouseY in y..y + height) {
                dragging = true
                lastPosition = Vec2f(mouseX - x, mouseY - y)
            }
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseReleased(mouseX, mouseY, click)

        dragging = false
    }

}