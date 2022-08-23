package com.paragon.client.ui.configuration.zeroday.element.setting

import com.paragon.Paragon
import com.paragon.api.event.client.SettingUpdateEvent
import com.paragon.api.setting.Bind
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.RenderUtil.drawRect
import com.paragon.api.util.render.RenderUtil.popScissor
import com.paragon.api.util.render.RenderUtil.pushScissor
import com.paragon.api.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.api.util.render.font.FontUtil.getStringWidth
import com.paragon.api.util.string.StringUtil.getFormattedText
import com.paragon.client.ui.configuration.zeroday.element.Element
import com.paragon.client.ui.configuration.zeroday.element.module.ModuleElement
import com.paragon.client.ui.util.Click
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.util.math.MathHelper
import java.awt.Color
import java.util.function.Consumer

class EnumElement(layer: Int, setting: Setting<Enum<*>?>, moduleElement: ModuleElement, x: Float, y: Float, width: Float, height: Float) : Element(layer, x, y, width, height) {
    val setting: Setting<Enum<*>?>
    private val scrollAnimation = Animation({ 1250f }, false) { Easing.LINEAR }

    init {
        this.parent = moduleElement.parent

        this.setting = setting
        setting.subsettings.forEach(Consumer { subsetting: Setting<*> ->
            if (subsetting.value is Boolean) {
                subElements.add(BooleanElement(layer + 1, (subsetting as Setting<Boolean?>), moduleElement, x, y, width, height))
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
            drawRect(x, y, width, height, Color(40, 40, 45).rgb)
            drawRect(x + layer, y, width - layer * 2, height, Color((40 + 30 * hover.getAnimationFactor()).toInt(), (40 + 30 * hover.getAnimationFactor()).toInt(), (45 + 30 * hover.getAnimationFactor()).toInt()).rgb)
            var x = x + layer * 2 + 5
            val totalWidth = width - layer * 2
            val maxTextWidth = totalWidth - getStringWidth(getFormattedText(setting.value!!)) - 5
            val visibleX = getStringWidth(setting.name) - maxTextWidth
            scrollAnimation.state = isHovered(mouseX, mouseY)

            if (getStringWidth(setting.name) > maxTextWidth) {
                x -= ((visibleX + 9) * scrollAnimation.getAnimationFactor()).toFloat()
            }

            val scissorY = MathHelper.clamp(
                y, parent.y + 22, parent.y + MathHelper.clamp( // Scissor comedy
                    parent.scissorHeight + 8, 0f, 358f
                )
            )

            val scissorHeight = height
            pushScissor((x + layer * 2).toDouble(), scissorY.toDouble(), (totalWidth - (getStringWidth(getFormattedText(setting.value!!)) + 9)).toDouble(), scissorHeight.toDouble())
            drawStringWithShadow(setting.name, x, y + height / 2 - 3.5f, -0x1)
            popScissor()
            drawStringWithShadow(getFormattedText(setting.value!!), x + width - layer * 2 - getStringWidth(getFormattedText(setting.value!!)) - (3f + if (subElements.isEmpty()) 0f else getStringWidth("...") + 3f), y + height / 2 - 3.5f, -0x1)

            if (subElements.isNotEmpty()) {
                drawStringWithShadow("...", x + width - getStringWidth("...") - 5, y + 2f, -1)
            }

            super.render(mouseX, mouseY, dWheel)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, click: Click) {
        if (setting.isVisible()) {
            if (isHovered(mouseX, mouseY) && parent.isElementVisible(this) && click == Click.LEFT) {
                setting.setValue(setting.nextMode)
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