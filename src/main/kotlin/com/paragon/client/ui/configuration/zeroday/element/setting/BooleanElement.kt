package com.paragon.client.ui.configuration.zeroday.element.setting

import com.paragon.Paragon
import com.paragon.api.event.client.SettingUpdateEvent
import com.paragon.api.setting.Bind
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.RenderUtil.drawRect
import com.paragon.api.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.api.util.render.font.FontUtil.getStringWidth
import com.paragon.client.ui.configuration.zeroday.element.Element
import com.paragon.client.ui.configuration.zeroday.element.module.ModuleElement
import com.paragon.client.ui.util.Click
import me.surge.animation.Animation
import me.surge.animation.Easing
import java.awt.Color
import java.util.function.Consumer

class BooleanElement(layer: Int, setting: Setting<Boolean?>, moduleElement: ModuleElement, x: Float, y: Float, width: Float, height: Float) : Element(layer, x, y, width, height) {
    val setting: Setting<Boolean?>
    private val enabledAnimation = Animation({ 200f }, false) { Easing.LINEAR }

    init {
        parent = moduleElement.parent
        this.setting = setting
        setting.subsettings.forEach(Consumer { subsetting: Setting<*> ->
            if (subsetting.value is Boolean) {
                subElements.add(BooleanElement(layer + 1, subsetting as Setting<Boolean?>, moduleElement, x, y, width, height))
            }
            else if (subsetting.value is Enum<*>) {
                subElements.add(EnumElement(layer + 1, subsetting as Setting<Enum<*>?>, moduleElement, x, y, width, height))
            }
            else if (subsetting.value is Number) {
                subElements.add(SliderElement(layer + 1, subsetting as Setting<Number?>, moduleElement, x, y, width, height))
            }
            else if (subsetting.value is Bind) {
                subElements.add(BindElement(layer + 1, (subsetting as Setting<Bind?>), moduleElement, x, y, width, height))
            }
            else if (subsetting.value is Color) {
                subElements.add(ColourElement(layer + 1, subsetting as Setting<Color>, moduleElement, x, y, width, height))
            }
            else if (subsetting.value is String) {
                subElements.add(StringElement(layer + 1, subsetting as Setting<String?>, moduleElement, x, y, width, height))
            }
        })
    }

    override fun render(mouseX: Int, mouseY: Int, dWheel: Int) {
        if (setting.isVisible()) {
            enabledAnimation.state = setting.value!!
            drawRect(x, y, width, height, Color(40, 40, 45).rgb)
            drawRect(x + layer, y, width - layer * 2, height, Color((40 + 30 * hover.getAnimationFactor()).toInt(), (40 + 30 * hover.getAnimationFactor()).toInt(), (45 + 30 * hover.getAnimationFactor()).toInt()).rgb)
            drawRect(x + layer, y, 1f, (height * enabledAnimation.getAnimationFactor()).toFloat(), Color.HSBtoRGB(parent.leftHue / 360, 1f, (0.5f + 0.25f * hover.getAnimationFactor()).toFloat()))
            drawStringWithShadow(setting.name, x + layer * 2 + 5, y + height / 2 - 3.5f, -0x1)
            if (!subElements.isEmpty()) {
                drawStringWithShadow("...", x + width - getStringWidth("...") - 5, y + 2f, -1)
            }
            super.render(mouseX, mouseY, dWheel)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, click: Click) {
        if (setting.isVisible()) {
            if (isHovered(mouseX, mouseY) && parent.isElementVisible(this) && click == Click.LEFT) {
                setting.setValue(!setting.value!!)
                Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
            }
            super.mouseClicked(mouseX, mouseY, click)
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, click: Click) {
        if (setting.isVisible()) {
            super.mouseReleased(mouseX, mouseY, click)
        }
    }

    override fun keyTyped(keyCode: Int, keyChar: Char) {
        if (setting.isVisible()) {
            super.keyTyped(keyCode, keyChar)
        }
    }

    override var height: Float
        get() = if (setting.isVisible()) super.height else 0f
        set(value) {}

    override fun getSubElementsHeight(): Float {
        return if (setting.isVisible()) super.getSubElementsHeight() else 0f
    }

    override fun getTotalHeight(): Float {
        return if (setting.isVisible()) super.getTotalHeight() else 0f
    }
}