package com.paragon.client.ui.configuration.window.element.setting.impl

import com.paragon.api.setting.Setting
import com.paragon.api.util.calculations.MathsUtil
import com.paragon.api.util.render.ColourUtil
import com.paragon.api.util.render.RenderUtil
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.window.element.setting.SettingElement
import com.paragon.client.ui.configuration.window.window.Window
import com.paragon.client.ui.util.Click
import net.minecraft.util.math.MathHelper
import org.lwjgl.input.Mouse
import java.awt.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/**
 * @author Wolfsurge
 */
class SliderElement(setting: Setting<Number>, window: Window, x: Float, y: Float, width: Float, height: Float) : SettingElement<Number>(setting, window, x, y, width, height) {

    var dragging: Boolean = false

    override fun draw(mouseX: Int, mouseY: Int) {
        var renderWidth = 0f
        val maxWidth = width - 4f

        if (setting.value is Float) {
            // Set values
            val diff = Math.min(maxWidth, Math.max(0f, mouseX - (x + 2)))
            val min = setting.min!!.toFloat()
            val max = setting.max!!.toFloat()
            renderWidth = maxWidth * (setting.value.toFloat() - min) / (max - min)
            if (!Mouse.isButtonDown(0)) {
                dragging = false
            }

            if (dragging) {
                if (diff == 0f) {
                    setting.setValue(setting.min!!)
                } else {
                    var newValue = MathsUtil.roundDouble((diff / maxWidth * (max - min) + min).toDouble(), 2).toFloat()
                    val precision = 1 / setting.incrementation!!.toFloat()
                    newValue = round(max(min, min(max, newValue)) * precision) / precision
                    setting.setValue(newValue)
                }
            }
        } else if (setting.value is Double) {
            // Set values
            val diff = min(maxWidth, max(0f, mouseX - (x + 2))).toDouble()

            val min = setting.min!!.toDouble()
            val max = setting.max!!.toDouble()

            renderWidth = (maxWidth * (setting.value.toDouble() - min) / (max - min)).toFloat()
            if (!Mouse.isButtonDown(0)) {
                dragging = false
            }

            if (dragging) {
                if (diff == 0.0) {
                    setting.setValue(setting.min!!)
                } else {
                    var newValue = MathsUtil.roundDouble(diff / maxWidth * (max - min) + min, 2)
                    val precision = (1 / setting.incrementation!!.toFloat()).toDouble()
                    newValue = Math.round(Math.max(min, Math.min(max, newValue)) * precision) / precision
                    setting.setValue(newValue)
                }
            }
        }

        RenderUtil.drawRect(x, y, width, height, Color((75 + (20 * hoverAnimation.getAnimationFactor())).toInt(), (75 + (20 * hoverAnimation.getAnimationFactor())).toInt(), (75 + (20 * hoverAnimation.getAnimationFactor())).toInt()).rgb)

        RenderUtil.drawRoundedRect(x + 2.0, y + height - 4.0, maxWidth.toDouble(), 3.0, 3.0, 3.0, 3.0, 3.0, Color(60, 60, 65).rgb)

        RenderUtil.drawRoundedRect(x + 2.0, y + height - 4.0, MathHelper.clamp(renderWidth, 2f, maxWidth).toDouble(), 3.0, 3.0, 3.0, 3.0, 3.0, Colours.mainColour.value.rgb)
        
        RenderUtil.drawRoundedRect(MathHelper.clamp(x + renderWidth, x + 2f, x + maxWidth - 1f).toDouble(), y + height - 4.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, -1)

        renderText(setting.name, x + 2, y + 5f, -1)
        renderText(setting.value.toString(), x + width - getStringWidth(setting.value.toString()) - 3, y + 5f, Color(185, 185, 190).rgb)

        super.draw(mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Click) {
        super.mouseClicked(mouseX, mouseY, button)

        if (isHovered(mouseX, mouseY)) {
            if (button == Click.LEFT) {
                dragging = true
            }

            else if (button == Click.RIGHT) {
                expandAnimation.state = !expandAnimation.state
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, button: Click) {
        super.mouseReleased(mouseX, mouseY, button)
        dragging = false
    }

}