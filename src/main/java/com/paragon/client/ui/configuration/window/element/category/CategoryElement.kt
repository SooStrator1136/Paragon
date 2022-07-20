package com.paragon.client.ui.configuration.window.element.category

import com.paragon.Paragon
import com.paragon.api.module.Category
import com.paragon.api.util.render.RenderUtil
import com.paragon.client.ui.configuration.window.element.Element
import com.paragon.client.ui.configuration.window.element.module.ModuleElement
import com.paragon.client.ui.configuration.window.window.Window
import com.paragon.client.ui.util.Click
import java.awt.Color

/**
 * @author Wolfsurge
 */
class CategoryElement(val category: Category, window: Window, x: Float, y: Float, width: Float, height: Float) : Element(window, x, y, width, height) {

    init {
        var offset = window.y + 30f

        Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it.category == category }.forEach {
            subElements.add(ModuleElement(it, window, x, offset, 95f, height))

            offset += height
        }
    }

    override fun draw(mouseX: Int, mouseY: Int) {
        super.draw(mouseX, mouseY)

        RenderUtil.drawRect(x, y, width, 16f, Color((75 + (20 * hoverAnimation.getAnimationFactor())).toInt(), (75 + (20 * hoverAnimation.getAnimationFactor())).toInt(), (75 + (20 * hoverAnimation.getAnimationFactor())).toInt()).rgb)
        renderText(category.Name, x + 2, y + 5f, -1)
    }

    fun getTotalWidth(): Float = (width + (subElements[0].width.toDouble() * this.expandAnimation.getAnimationFactor())).toFloat()

    fun renderModuleComponents(mouseX: Int, mouseY: Int, scrollIn: Float) {
        if (expandAnimation.getAnimationFactor() > 0) {
            var offset = window.y + 30f

            subElements.forEach {
                it.updatePosition((window.x + 2 + (95f * expandAnimation.getAnimationFactor())).toFloat(), offset + scrollIn)
                it.draw(mouseX, mouseY)

                offset += it.getTotalHeight()
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Click) {
        super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, button: Click) {
        super.mouseReleased(mouseX, mouseY, button)
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        super.keyTyped(character, keyCode)
    }

}