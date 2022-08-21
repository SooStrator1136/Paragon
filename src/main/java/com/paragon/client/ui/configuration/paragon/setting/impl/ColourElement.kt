package com.paragon.client.ui.configuration.paragon.setting.impl

import com.paragon.api.setting.Setting
import com.paragon.api.util.calculations.MathsUtil
import com.paragon.api.util.calculations.MathsUtil.roundDouble
import com.paragon.api.util.render.ColourUtil.fade
import com.paragon.api.util.render.ColourUtil.integrateAlpha
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.RenderUtil.drawBorder
import com.paragon.api.util.render.font.FontUtil
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.paragon.module.ModuleElement
import com.paragon.client.ui.configuration.paragon.setting.SettingElement
import com.paragon.client.ui.util.Click
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11.GL_FLAT
import org.lwjgl.opengl.GL11.GL_SMOOTH
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

    //TODO add SettingUpdateEvent

    private val hue = HueSlider(x, y + 100, width, 9f)
    private val alpha = AlphaSlider(x, y + 110, width, 9f)

    private val rainbow = Setting("Rainbow", setting.isRainbow)
    private val rainbowSaturation = Setting("Saturation", setting.rainbowSaturation, 0f, 100f, 1f) subOf rainbow
    private val rainbowSpeed = Setting("Speed", setting.rainbowSpeed, 0.1f, 10f, 0.1f) subOf rainbow
    private val sync = Setting("Sync", setting.isSync)

    private var finalColour: Color? = null

    private var isPicking = false

    init {
        val values = Color.RGBtoHSB(setting.value.red, setting.value.green, setting.value.blue, null)

        hue.hue = values[0] * 360f
        alpha.alpha = 255 - setting.alpha
        subElements.add(BooleanElement(rainbow, module, x, y, width, height))
        subElements.add(BooleanElement(sync, module, x, y, width, height))

        finalColour = setting.value
    }

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        hover.state = isHovered(mouseX, mouseY)

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
            val hueValue = hue.hue / 360f

            val colour = Color(Color.HSBtoRGB(hueValue, 1f, 1f))

            // GL shit pt 1
            GlStateManager.pushMatrix()
            GlStateManager.disableTexture2D()
            GlStateManager.enableBlend()
            GlStateManager.disableAlpha()
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
            GlStateManager.shadeModel(GL_SMOOTH)

            // Get tessellator and buffer builder
            val tessellator = Tessellator.getInstance()
            val bufferBuilder = tessellator.buffer

            val dimension = 80f

            // Add positions
            bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)
            bufferBuilder.pos(((x + width / 2 - dimension / 2) + dimension).toDouble(), (y + 17).toDouble(), 0.0).color(colour.red, colour.green, colour.blue, colour.alpha).endVertex()
            bufferBuilder.pos((x + width / 2 - dimension / 2).toDouble(), (y + 17).toDouble(), 0.0).color(255, 255, 255, 255).endVertex()
            bufferBuilder.pos((x + width / 2 - dimension / 2).toDouble(), ((y + 17) + dimension + 2).toDouble(), 0.0).color(0, 0, 0, 255).endVertex()
            bufferBuilder.pos(((x + width / 2 - dimension / 2) + dimension).toDouble(), ((y + 17) + dimension + 2).toDouble(), 0.0).color(0, 0, 0, 255).endVertex()

            // Draw rect
            tessellator.draw()

            // GL shit pt 2
            GlStateManager.shadeModel(GL_FLAT)
            GlStateManager.enableAlpha()
            GlStateManager.enableTexture2D()
            GlStateManager.popMatrix()

            drawBorder((x + width / 2 - dimension / 2), (y + 17), dimension, dimension + 2, 0.5f, -1)

            // Check we are dragging

            if (isPicking) {
                val satDiff = min(dimension, max(0f, mouseX - (x + width / 2 - dimension / 2)))

                val saturation = if (satDiff == 0f) {
                    0f
                } else {
                    roundDouble((satDiff / dimension * 100).toDouble(), 0).toFloat()
                } / 100f

                val brightDiff = min(dimension, max(0f, ((y + 17) + dimension) - mouseY))

                val brightness = if (brightDiff == 0f) {
                    0f
                } else {
                    roundDouble((brightDiff / dimension * 100).toDouble(), 0).toFloat()
                } / 100f

                finalColour = Color(Color.HSBtoRGB(hueValue, saturation, brightness)).integrateAlpha(255 - alpha.alpha)
            }

            if (hue.dragging) {
                val hsb = Color.RGBtoHSB(finalColour!!.red, finalColour!!.green, finalColour!!.blue, null)
                finalColour = Color(Color.HSBtoRGB(hue.hue.toInt() / 360f, hsb[1], hsb[2]))

                isPicking = false
            }

            hue.x = x + 4
            hue.y = y + height + 90
            hue.width = width - 8
            hue.height = 9f

            hue.draw(mouseX, mouseY)

            alpha.x = x + 4
            alpha.y = y + height + 100
            alpha.width = width - 8
            alpha.height = 9f

            alpha.draw(colour, mouseX, mouseY)

            var offset = y + height + 110

            subElements.forEach {
                it.x = x
                it.y = offset

                it.draw(mouseX, mouseY, mouseDelta)

                offset += it.getAbsoluteHeight()
            }

            RenderUtil.drawRect(x + 1, y + height, 1f, offset - y - height, Colours.mainColour.value.rgb)

            // Get final HSB colours
            val finHSB = Color.RGBtoHSB(setting.value.red, setting.value.green, setting.value.blue, null)

            // Picker X and Y
            val pickerX = (x + width / 2 - dimension / 2) + finHSB[1] * dimension
            val pickerY = (y + 17) + ((1 - finHSB[2]) * dimension)

            // Draw picker highlight
            RenderUtil.drawRect(pickerX - 1.5f, pickerY - 1.5f, 3f, 3f, -1)
            RenderUtil.drawRect(pickerX - 1, pickerY - 1, 2f, 2f, finalColour!!.rgb)
        }

        setting.alpha = 255 - alpha.alpha
        setting.isRainbow = rainbow.value
        setting.rainbowSaturation = rainbowSaturation.value
        setting.rainbowSpeed = rainbowSpeed.value
        setting.isSync = sync.value
        setting.setValue(finalColour!!)

        RenderUtil.drawRect(x + width - 7, y, 7f, height, Color(37, 42, 51, 100).rgb)

        // lel
        FontUtil.defaultFont.drawStringWithShadow(".", x + width - 6, y - 5, -1)
        FontUtil.defaultFont.drawStringWithShadow(".", x + width - 6, y - 1, -1)
        FontUtil.defaultFont.drawStringWithShadow(".", x + width - 6, y + 3, -1)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (expanded.state) {
            hue.mouseClicked(mouseX, mouseY, click)
            alpha.mouseClicked(mouseX, mouseY, click)

            if (mouseX in ((x + width / 2 - 80 / 2)..((x + width / 2 - 80 / 2) + 80)) && mouseY in ((y + 17)..((y + 17) + 80))) {
                isPicking = true
            }
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseReleased(mouseX, mouseY, click)

        isPicking = false

        if (expanded.state) {
            hue.mouseReleased(mouseX, mouseY)
            alpha.mouseReleased(mouseX, mouseY)
        }
    }

    override fun getAbsoluteHeight(): Float {
        return (height + ((110 + (subElements.sumOf { it.getAbsoluteHeight().toInt() })) * expanded.getAnimationFactor())).toFloat()
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

    private class AlphaSlider(var x: Float, var y: Float, var width: Float, var height: Float) {
        var dragging = false
        var alpha = 0f

        fun draw(colour: Color, mouseX: Float, mouseY: Float) {
            RenderUtil.drawHorizontalGradientRect(x, y, width + 9, height, colour.integrateAlpha(255f).rgb, 0)

            // Set values
            val diff = min(width, max(0f, mouseX - x))

            if (!Mouse.isButtonDown(0)) {
                dragging = false
            }

            if (dragging) {
                if (diff == 0f) {
                    alpha = 0f
                } else {
                    var newValue = roundDouble((diff / width * 255).toDouble(), 2).toFloat()
                    newValue = ((round((max(0f, min(255f, newValue)) * 1).toDouble()) / 1).toFloat())

                    alpha = roundDouble(newValue.toDouble(), BigDecimal.valueOf(1).scale()).toFloat()
                }
            }

            val sliderMinX: Int = (x + (width * (alpha / 255f))).toInt()
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