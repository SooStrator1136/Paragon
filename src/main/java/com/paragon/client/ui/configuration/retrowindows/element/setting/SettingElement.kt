package com.paragon.client.ui.configuration.retrowindows.element.setting

import com.paragon.api.setting.Bind
import com.paragon.api.setting.Setting
import com.paragon.client.systems.module.impl.client.ClickGUI
import com.paragon.client.ui.configuration.retrowindows.element.RawElement
import com.paragon.client.ui.configuration.retrowindows.element.module.ModuleElement
import com.paragon.client.ui.configuration.retrowindows.element.setting.elements.*
import com.paragon.client.ui.util.Click
import com.paragon.client.ui.util.animation.Animation
import java.awt.Color

/**
 * @author Surge
 */
abstract class SettingElement<T>(val parent: ModuleElement, val setting: Setting<T>, x: Float, y: Float, width: Float, height: Float) : RawElement(x, y, width, height) {

    val expanded: Animation = Animation( { ClickGUI.animationSpeed.value }, false, { ClickGUI.easing.value })
    val subsettings = ArrayList<SettingElement<*>>()

    init {
        setting.subsettings.forEach {
            if (it.value is Boolean) {
                subsettings.add(BooleanElement(parent, it as Setting<Boolean>, x + 2, y, width - 4, height))
            }

            else if (it.value is Enum<*>) {
                subsettings.add(EnumElement(parent, it as Setting<Enum<*>>, x + 2, y, width - 4, height))
            }

            else if (it.value is Number) {
                subsettings.add(SliderElement(parent, it as Setting<Number>, x + 2, y, width - 4, height))
            }

            else if (it.value is Bind) {
                subsettings.add(BindElement(parent, it as Setting<Bind>, x + 2, y, width - 4, height))
            }

            else if (it.value is String) {
                subsettings.add(StringElement(parent, it as Setting<String>, x + 2, y, width - 4, height))
            }

            else if (it.value is Color) {
                subsettings.add(ColourElement(parent, it as Setting<Color>, x + 2, y, width - 4, height))
            }
        }
    }

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        if (isHovered(mouseX, mouseY)) {
            parent.parent.tooltipName = setting.name
            parent.parent.tooltipContent = setting.description
        }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (expanded.state) {
            subsettings.forEach {
                if (it.setting.isVisible()) {
                    it.mouseClicked(mouseX, mouseY, click)
                }
            }
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseReleased(mouseX, mouseY, click)

        if (expanded.state) {
            subsettings.forEach {
                if (it.setting.isVisible()) {
                    it.mouseReleased(mouseX, mouseY, click)
                }
            }
        }
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        super.keyTyped(character, keyCode)

        if (expanded.state) {
            subsettings.forEach {
                if (it.setting.isVisible()) {
                    it.keyTyped(character, keyCode)
                }
            }
        }
    }

    open fun getSubsettingHeight(): Float {
        var height = 0f

        subsettings.forEach {
            if (it.setting.isVisible()) {
                height += it.getTotalHeight()
            }
        }

        return height
    }

    fun getTotalHeight(): Float {
        return (height + (getSubsettingHeight() * expanded.getAnimationFactor())).toFloat()
    }

}