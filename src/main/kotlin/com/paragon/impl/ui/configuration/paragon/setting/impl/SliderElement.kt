package com.paragon.impl.ui.configuration.paragon.setting.impl

import com.paragon.Paragon
import com.paragon.impl.event.client.SettingUpdateEvent
import com.paragon.impl.setting.Setting
import com.paragon.util.render.ColourUtil.fade
import com.paragon.util.render.font.FontUtil
import com.paragon.impl.module.client.Colours
import com.paragon.impl.ui.configuration.paragon.module.ModuleElement
import com.paragon.impl.ui.configuration.paragon.setting.SettingElement
import com.paragon.impl.ui.util.Click
import com.paragon.util.calculations.MathsUtil
import com.paragon.util.render.RenderUtil
import org.lwjgl.input.Mouse
import java.awt.Color
import java.math.BigDecimal
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.properties.Delegates

/**
 * @author Surge
 * @since 06/08/2022
 */
class SliderElement(setting: Setting<Number>, module: ModuleElement, x: Float, y: Float, width: Float, height: Float) : SettingElement<Number>(setting, module, x, y, width, height) {

    var dragging = false

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        RenderUtil.drawRect(
            x, y, width, height, Color(53, 53, 74).fade(Color(64, 64, 92), hover.getAnimationFactor()).rgb
        )

        RenderUtil.scaleTo(x + 5, y + 7, 0f, 0.5, 0.5, 0.5) {
            if (hover.getAnimationFactor() > 0.5) {
                FontUtil.drawStringWithShadow(
                    "${setting.value} / ${setting.max}", x + 5, y + 7 + (6 * hover.getAnimationFactor()).toFloat(), Color.GRAY.rgb
                )
            }
        }

        RenderUtil.scaleTo(x + 5, y + 7, 0f, 0.7, 0.7, 0.7) {
            val factor = 1 / 0.7f

            val valueX = x + width * factor - FontUtil.getStringWidth(setting.value.toString()) - 12

            FontUtil.drawStringWithShadow(
                setting.value.toString(), valueX, y + 1, Color.GRAY.brighter().fade(Color.GRAY.brighter().brighter(), hover.getAnimationFactor()).rgb
            )
        }

        RenderUtil.scaleTo(x + 5, y + 5, 0f, 0.7, 0.7, 0.7) {
            FontUtil.drawStringWithShadow(setting.name, x + 5, y + 5 - (3 * hover.getAnimationFactor()).toFloat(), -1)
        }

        var renderWidth by Delegates.notNull<Float>()
        val maxWidth = width - 12

        if (setting.value is Float) {
            // Set values
            val diff = min(maxWidth, max(0f, mouseX - (x + 4)))

            val min = setting.min.toFloat()
            val max = setting.max.toFloat()

            renderWidth = (maxWidth * (setting.value.toDouble() - min) / (max - min)).toFloat()

            if (!Mouse.isButtonDown(0)) {
                dragging = false
            }

            if (dragging) {
                if (diff == 0f) {
                    setting.setValue(setting.min)
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                }
                else {
                    var newValue = MathsUtil.roundDouble((diff / maxWidth * (max - min) + min).toDouble(), 2).toFloat()
                    val precision = 1 / setting.incrementation.toFloat()
                    newValue = (round(max(min, min(max, newValue)) * precision) / precision)

                    setting.setValue(
                        MathsUtil.roundDouble(
                            newValue.toDouble(), BigDecimal.valueOf(setting.incrementation.toDouble()).scale()
                        ).toFloat()
                    )
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                }
            }
        }
        else if (setting.value is Double) {
            // Set values
            val diff = min(maxWidth, max(0f, mouseX - (x + 4))).toDouble()

            val min = setting.min.toDouble()
            val max = setting.max.toDouble()

            renderWidth = (maxWidth * (setting.value.toDouble() - min) / (max - min)).toFloat()

            if (!Mouse.isButtonDown(0)) {
                dragging = false
            }

            if (dragging) {
                if (diff == 0.0) {
                    setting.setValue(setting.min)
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                }
                else {
                    var newValue = MathsUtil.roundDouble(diff / maxWidth * (max - min) + min, 2)
                    val precision = (1 / setting.incrementation.toFloat()).toDouble()

                    newValue = round(max(min, min(max, newValue)) * precision) / precision

                    setting.setValue(
                        MathsUtil.roundDouble(
                            newValue, BigDecimal.valueOf(setting.incrementation.toDouble()).scale()
                        )
                    )
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                }
            }
        }

        RenderUtil.drawRect(x + 4, y + height - 1, maxWidth, 1f, Color.GRAY.rgb)
        RenderUtil.drawRect(x + 4, y + height - 1, renderWidth, 1f, Colours.mainColour.value.rgb)

        super.draw(mouseX, mouseY, mouseDelta)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (click == Click.LEFT && hover.state) {
            dragging = true
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseReleased(mouseX, mouseY, click)
        dragging = false
    }

}