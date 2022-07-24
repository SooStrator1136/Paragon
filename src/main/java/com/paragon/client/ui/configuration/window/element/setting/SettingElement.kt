package com.paragon.client.ui.configuration.window.element.setting

import com.paragon.api.setting.Setting
import com.paragon.client.ui.configuration.window.element.Element
import com.paragon.client.ui.configuration.window.element.setting.impl.BooleanElement
import com.paragon.client.ui.configuration.window.element.setting.impl.EnumElement
import com.paragon.client.ui.configuration.window.element.setting.impl.SliderElement
import com.paragon.client.ui.configuration.window.window.Window

/**
 * @author Wolfsurge
 */
open class SettingElement<T>(val setting: Setting<T>, window: Window, x: Float, y: Float, width: Float, height: Float) : Element(window, x, y, width, height) {

    init {
        setting.subsettings.forEach {
            when (it.value) {
                is Boolean -> subElements.add(BooleanElement(it as Setting<Boolean>, window, x, y, width, height))
                is Enum<*> -> subElements.add(EnumElement(it as Setting<Enum<*>>, window, x, y, width, height))
                is Number -> subElements.add(SliderElement(it as Setting<Number>, window, x, y, width, height + 4))
            }
        }
    }

    override fun draw(mouseX: Int, mouseY: Int) {
        super.draw(mouseX, mouseY)

        if (expandAnimation.getAnimationFactor() > 0) {
            var y = this.y + height

            subElements.forEach {
                it.updatePosition(x, y)

                it.draw(mouseX, mouseY)

                y += it.getTotalHeight()
            }
        }
    }

}