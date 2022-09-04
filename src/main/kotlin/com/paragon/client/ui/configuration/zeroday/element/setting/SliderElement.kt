package com.paragon.client.ui.configuration.zeroday.element.setting

import com.paragon.Paragon
import com.paragon.api.event.client.SettingUpdateEvent
import com.paragon.api.setting.Bind
import com.paragon.api.setting.Setting
import com.paragon.api.util.calculations.MathsUtil.roundDouble
import com.paragon.api.util.render.RenderUtil.drawRect
import com.paragon.api.util.render.RenderUtil.popScissor
import com.paragon.api.util.render.RenderUtil.pushScissor
import com.paragon.api.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.api.util.render.font.FontUtil.getStringWidth
import com.paragon.client.ui.configuration.zeroday.element.Element
import com.paragon.client.ui.configuration.zeroday.element.module.ModuleElement
import com.paragon.client.ui.util.Click
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.util.math.MathHelper
import org.lwjgl.input.Mouse
import java.awt.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class SliderElement(
    layer: Int,
    setting: Setting<Number>,
    moduleElement: ModuleElement,
    x: Float,
    y: Float,
    width: Float,
    height: Float
) : Element(layer, x, y, width, height) {

    override var height: Float
        get() = if (setting.isVisible()) super.height else 0f
        set(value) {}

    val setting: Setting<Number>
    var isDragging = false
        private set

    private val scrollAnimation = Animation({ 1250f }, false) { Easing.LINEAR }

    init {
        parent = moduleElement.parent
        this.setting = setting
        setting.subsettings.forEach {
            when (it.value) {
                is Boolean -> subElements.add(
                    BooleanElement(
                        layer + 1,
                        (it as Setting<Boolean>),
                        moduleElement,
                        x,
                        y,
                        width,
                        height
                    )
                )

                is Enum<*> -> subElements.add(
                    EnumElement(
                        layer + 1,
                        (it as Setting<Enum<*>>),
                        moduleElement,
                        x,
                        y,
                        width,
                        height
                    )
                )

                is Number -> subElements.add(
                    SliderElement(
                        layer + 1,
                        it as Setting<Number>,
                        moduleElement,
                        x,
                        y,
                        width,
                        height
                    )
                )

                is Bind -> subElements.add(
                    BindElement(
                        layer + 1,
                        (it as Setting<Bind>),
                        moduleElement,
                        x,
                        y,
                        width,
                        height
                    )
                )

                is Color -> subElements.add(
                    ColourElement(
                        layer + 1,
                        it as Setting<Color>,
                        moduleElement,
                        x,
                        y,
                        width,
                        height
                    )
                )

                is String -> subElements.add(
                    StringElement(
                        layer + 1,
                        it as Setting<String>,
                        moduleElement,
                        x,
                        y,
                        width,
                        height
                    )
                )
            }
        }
    }

    override fun render(mouseX: Int, mouseY: Int, dWheel: Int) {
        if (setting.isVisible()) {
            var renderWidth = 0f
            val maxWidth = width - layer * 2
            if (setting.value is Float) {
                // Set values
                val diff = min(maxWidth, max(0f, mouseX - (x + layer)))

                val min = setting.min.toFloat()
                val max = setting.max.toFloat()

                renderWidth = maxWidth * ((setting.value as Float) - min) / (max - min)

                if (!Mouse.isButtonDown(0)) {
                    isDragging = false
                }
                if (isDragging) {
                    if (diff == 0f) {
                        setting.setValue(setting.min)
                        Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                    } else {
                        var newValue = roundDouble((diff / maxWidth * (max - min) + min).toDouble(), 2).toFloat()
                        val precision = 1 / setting.incrementation.toFloat()

                        newValue = (max(min, min(max, newValue)) * precision).roundToInt() / precision

                        setting.setValue(newValue)
                        Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                    }
                }
            } else if (setting.value is Double) {
                // Set values
                val diff = min(maxWidth, max(0f, mouseX - (x + layer))).toDouble()

                val min = setting.min.toDouble()
                val max = setting.max.toDouble()

                renderWidth = (maxWidth * ((setting.value as Double) - min) / (max - min)).toFloat()

                if (!Mouse.isButtonDown(0)) {
                    isDragging = false
                }
                if (isDragging) {
                    if (diff == 0.0) {
                        setting.setValue(setting.min)
                        Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                    } else {
                        var newValue = roundDouble(diff / maxWidth * (max - min) + min, 2)
                        val precision = (1 / setting.incrementation.toFloat()).toDouble()
                        newValue = (max(min, min(max, newValue)) * precision).roundToInt() / precision
                        setting.setValue(newValue)
                        Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                    }
                }
            }

            drawRect(x, y, width, height, Color(40, 40, 45).rgb)

            drawRect(
                x + layer,
                y,
                width - layer * 2,
                height,
                Color(
                    (40 + 30 * hover.getAnimationFactor()).toInt(),
                    (40 + 30 * hover.getAnimationFactor()).toInt(),
                    (45 + 30 * hover.getAnimationFactor()).toInt()
                ).rgb
            )

            drawRect(
                x + layer,
                y,
                renderWidth,
                height,
                Color.HSBtoRGB(parent.leftHue / 360, 1f, (0.5f + 0.25f * hover.getAnimationFactor()).toFloat())
            )

            val x = x + layer * 2 + 5
            val totalWidth = width - layer * 2
            val maxTextWidth = totalWidth - getStringWidth(setting.value.toString()) - 12
            val visibleX = getStringWidth(setting.name) - maxTextWidth
            scrollAnimation.state = isHovered(mouseX, mouseY)

            pushScissor(x.toDouble(), MathHelper.clamp(y.toDouble(), parent.y + parent.height.toDouble(), 100000.0), maxTextWidth.toDouble(), ((parent.y + parent.height) + parent.scissorHeight) - y.toDouble())

            drawStringWithShadow(setting.name, x - if (getStringWidth(setting.name) > maxTextWidth) ((visibleX + 9) * scrollAnimation.getAnimationFactor()).toFloat() else 0f, y + height / 2 - 3.5f, -0x1)

            popScissor()

            drawStringWithShadow(
                setting.value.toString(),
                (x + totalWidth) - getStringWidth(setting.value.toString()) - 10,
                y + height / 2 - 3.5f,
                -0x1
            )

            super.render(mouseX, mouseY, dWheel)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, click: Click) {
        if (setting.isVisible()) {
            if (isHovered(mouseX, mouseY) && parent.isElementVisible(this) && click == Click.LEFT) {
                isDragging = true
            }
            super.mouseClicked(mouseX, mouseY, click)
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, click: Click) {
        isDragging = false
        if (setting.isVisible()) {
            super.mouseReleased(mouseX, mouseY, click)
        }
    }

    override fun keyTyped(keyCode: Int, keyChar: Char) {
        if (setting.isVisible()) {
            super.keyTyped(keyCode, keyChar)
        }
    }

    override fun getSubElementsHeight(): Float {
        return if (setting.isVisible()) super.getSubElementsHeight() else 0f
    }

    override fun getTotalHeight(): Float {
        return if (setting.isVisible()) super.getTotalHeight() else 0f
    }
}