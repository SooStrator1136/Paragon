package com.paragon.client.ui.configuration.window.element

import com.paragon.api.util.render.ITextRenderer
import com.paragon.client.systems.module.impl.client.ClickGUI
import com.paragon.client.ui.configuration.window.window.Window
import com.paragon.client.ui.util.Click
import com.paragon.client.ui.util.animation.Animation
import com.paragon.client.ui.util.animation.Easing

/**
 * @author Wolfsurge
 * @since 17/07/22
 */
abstract class Element(val window: Window, var x: Float, var y: Float, var width: Float, var height: Float) : ITextRenderer {

    val subElements: ArrayList<Element> = ArrayList()
    val expandAnimation: Animation = Animation({ ClickGUI.animationSpeed.value }, false, { ClickGUI.easing.value })

    val hoverAnimation = Animation({ 300f }, false, { Easing.LINEAR })

    /**
     * Draws the component.
     *
     * @param mouseX The mouse's x position.
     * @param mouseY The mouse's y position.
     */
    open fun draw(mouseX: Int, mouseY: Int) {
        hoverAnimation.state = isHovered(mouseX, mouseY)
    }

    /**
     * Called when the mouse is clicked.
     * Ideally would be abstract, but as I don't know of a method to include the code for
     * invoking this same method on all the sub elements, it just has to be overridden and the super.mouseClicked() method called.
     * Same goes for the below methods.
     *
     * @param mouseX The mouse's x position.
     * @param mouseY The mouse's y position.
     * @param button The mouse button that was clicked
     */
    open fun mouseClicked(mouseX: Int, mouseY: Int, button: Click) {
        if (expandAnimation.state) {
            subElements.forEach { it.mouseClicked(mouseX, mouseY, button) }
        }
    }

    /**
     * Called when the mouse is released.
     *
     * @param mouseX The mouse's x position.
     * @param mouseY The mouse's y position.
     * @param button The mouse button that was released.
     */
    open fun mouseReleased(mouseX: Int, mouseY: Int, button: Click) {
        if (expandAnimation.state) {
            subElements.forEach { it.mouseReleased(mouseX, mouseY, button) }
        }
    }

    /**
     * Called when a keyboard character is pressed.
     *
     * @param character The character that was pressed.
     * @param keyCode The key code of the character.
     */
    open fun keyTyped(character: Char, keyCode: Int) {
        if (expandAnimation.state) {
            subElements.forEach { it.keyTyped(character, keyCode) }
        }
    }

    /**
     * Updates the position of this element.
     *
     * @param x The new x position.
     * @param y The new y position.
     */
    fun updatePosition(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    /**
     * Checks whether the mouse is currently hovered over this element.
     *
     * @param mouseX The mouse's x position.
     * @param mouseY The mouse's y position.
     */
    fun isHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX.toFloat() in x..x + width && mouseY.toFloat() in y..y + height && y in window.y + 30..window.y + window.height - 33
    }

    /**
     * Gets the total height of the element.
     *
     * The total height is equal to the total height of each sub element, plus the height of the element itself.
     * This is used for setting the Y position of the element in the parent window.
     *
     * @return The total height of the element.
     */
    open fun getTotalHeight(): Float = height

}