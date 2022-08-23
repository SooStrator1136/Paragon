package com.paragon.client.ui.configuration.zeroday.element

import com.paragon.api.util.Wrapper
import com.paragon.client.systems.module.impl.client.ClickGUI.animationSpeed
import com.paragon.client.systems.module.impl.client.ClickGUI.easing
import com.paragon.client.ui.configuration.zeroday.panel.CategoryPanel
import com.paragon.client.ui.util.Click
import me.surge.animation.Animation
import me.surge.animation.Easing
import java.util.function.Consumer

abstract class Element(val layer: Int, var x: Float, var y: Float, var width: Float, open var height: Float) : Wrapper {
    open var lastX: Float = 0f
    open var lastY: Float = 0f

    val hover = Animation({ 200f }, false) { Easing.LINEAR }

    private val subelements: ArrayList<Element> = ArrayList()

    lateinit var parent: CategoryPanel

    val animation = Animation(animationSpeed::value, false, easing::value)

    private var open = false

    init {
        lastX = x
        lastY = y
    }

    open fun render(mouseX: Int, mouseY: Int, dWheel: Int) {
        hover.state = isHovered(mouseX, mouseY)
        if (animation.getAnimationFactor() > 0) {
            var offset = y + height
            for (subElement in subElements) {
                subElement.x = x
                subElement.y = offset
                subElement.render(mouseX, mouseY, dWheel)
                offset += subElement.getTotalHeight()
            }
        }

        else {
            for (subElement in subElements) {
                subElement.hover.state = false
            }
        }
    }

    open fun mouseClicked(mouseX: Int, mouseY: Int, click: Click) {
        if (isHovered(mouseX, mouseY) && parent != null && parent!!.isElementVisible(this)) {
            if (click == Click.RIGHT) {
                open = !open
                animation.state = open
                return
            }
        }

        if (open) {
            subElements.forEach(Consumer { subelement: Element -> subelement.mouseClicked(mouseX, mouseY, click) })
        }
    }

    open fun mouseReleased(mouseX: Int, mouseY: Int, click: Click) {
        if (open) {
            subElements.forEach(Consumer { subelement: Element -> subelement.mouseReleased(mouseX, mouseY, click) })
        }
    }

    open fun keyTyped(keyCode: Int, keyChar: Char) {
        if (open) {
            subElements.forEach(Consumer { subelement: Element -> subelement.keyTyped(keyCode, keyChar) })
        }
    }

    fun isHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= x && mouseX <= x + width && mouseY > y && mouseY <= y + height
    }

    open fun getSubElementsHeight(): Float {
        var height = 0f

        for (element in subelements) {
            height += element.getTotalHeight()
        }

        return height
    }

    open fun getTotalHeight(): Float {
        return (height + getSubElementsHeight() * animation.getAnimationFactor()).toFloat()
    }

    val subElements: ArrayList<Element>
        get() = subelements

    fun addSubElement(element: Element) {
        subelements.add(element)
    }
}