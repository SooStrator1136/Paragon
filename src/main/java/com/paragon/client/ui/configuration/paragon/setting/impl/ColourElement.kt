package com.paragon.client.ui.configuration.paragon.setting.impl

import com.paragon.api.setting.Setting
import com.paragon.api.util.calculations.MathsUtil
import com.paragon.api.util.render.ColourUtil.fade
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.font.FontUtil
import com.paragon.client.ui.configuration.paragon.module.ModuleElement
import com.paragon.client.ui.configuration.paragon.setting.SettingElement
import com.paragon.client.ui.util.Click
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.math.BigDecimal
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/**
 * @author Surge
 * @since 06/08/2022
 */
class ColourElement(setting: Setting<Color>, module: ModuleElement, x: Float, y: Float, width: Float, height: Float) : SettingElement<Color>(setting, module, x, y, width, height) {

    private val hue = HueSlider(x, y + 80, width, 9f)

    init {
        val values = Color.RGBtoHSB(setting.value.red, setting.value.green, setting.value.blue, null)

        hue.hue = values[0] * 360f
    }

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        RenderUtil.drawRect(x, y, width, getAbsoluteHeight(), Color(53, 53, 74).fade(Color(64, 64, 92), hover.getAnimationFactor()).rgb)

        RenderUtil.scaleTo(x + 5, y + 7, 0f, 0.5, 0.5, 0.5) {
            if (hover.getAnimationFactor() > 0.5) {
                FontUtil.drawStringWithShadow("R ${setting.value.red} G ${setting.value.green} B ${setting.value.blue} A ${setting.alpha}", x + 5, y + 7 + (7 * hover.getAnimationFactor()).toFloat(), Color.GRAY.rgb)
            }
        }

        RenderUtil.scaleTo(x + 5, y + 5, 0f, 0.7, 0.7, 0.7) {
            FontUtil.drawStringWithShadow(setting.name, x + 5, y + 5 - (3 * hover.getAnimationFactor()).toFloat(), Color.GRAY.brighter().fade(Color.WHITE, expanded.getAnimationFactor()).rgb)
        }

        if (expanded.getAnimationFactor() > 0) {
            hue.x = x + 4
            hue.y = (y + height + 120) - (hue.height + 4)
            hue.width = width - 8
            hue.height = 9f

            hue.draw(mouseX, mouseY)
        }

        super.draw(mouseX, mouseY, mouseDelta)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (expanded.state) {
            hue.mouseClicked(mouseX, mouseY, click)
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseReleased(mouseX, mouseY, click)

        if (expanded.state) {
            hue.mouseReleased(mouseX, mouseY)
        }
    }

    override fun getAbsoluteHeight(): Float {
        return (height + (120 * expanded.getAnimationFactor())).toFloat()
    }

    private class HueSlider(var x: Float, var y: Float, var width: Float, var height: Float) {
        var dragging = false
        var hue = 0f

        fun draw(mouseX: Float, mouseY: Float) {
            var step = 0

            for (colorIndex in 0..5) {
                val previousStep = Color.HSBtoRGB(step / 6f, 1.0f, 1.0f)
                val nextStep = Color.HSBtoRGB((step + 1f) / 6, 1.0f, 1.0f)

                RenderUtil.drawHorizontalGradientRect(x + step * (width / 6), y, width / 6, height, previousStep, nextStep)

                step += 1
            }

            // Set values
            val diff = min(width, max(0f, mouseX - x))

            val renderWidth = (width * hue / 360)

            if (!Mouse.isButtonDown(0)) {
                dragging = false
            }

            if (dragging) {
                if (diff == 0f) {
                    hue = 0f
                } else {
                    var newValue = MathsUtil.roundDouble((diff / width * 360).toDouble(), 2).toFloat()
                    newValue = ((round((max(0f, min(360f, newValue)) * 1).toDouble()) / 1).toFloat())

                    hue = MathsUtil.roundDouble(newValue.toDouble(), BigDecimal.valueOf(1).scale()).toFloat()
                }
            }

            val sliderMinX: Int = (x + (width * (hue / 360f))).toInt()
            RenderUtil.drawRect(sliderMinX - 1f, y, 1f, height, -1)
        }

        fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
            if (click == Click.LEFT) {
                if (mouseX in x..(x + width) && mouseY in y..(y + height)) {
                    dragging = true
                }
            }
        }

        fun mouseReleased(mouseX: Float, mouseY: Float) {
            dragging = false
        }
    }

}