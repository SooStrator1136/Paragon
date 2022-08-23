package com.paragon.client.ui.configuration.zeroday.element.setting

import com.paragon.api.setting.Setting
import com.paragon.api.util.calculations.MathsUtil.roundDouble
import com.paragon.api.util.render.RenderUtil.drawBorder
import com.paragon.api.util.render.RenderUtil.drawRect
import com.paragon.api.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.client.systems.module.impl.client.ClickGUI.animationSpeed
import com.paragon.client.systems.module.impl.client.ClickGUI.easing
import com.paragon.client.ui.configuration.zeroday.element.Element
import com.paragon.client.ui.configuration.zeroday.element.module.ModuleElement
import com.paragon.client.ui.util.Click
import me.surge.animation.Animation
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.math.MathHelper
import org.lwjgl.input.Mouse
import java.awt.Color
import java.util.function.Consumer

class ColourElement(layer: Int, setting: Setting<Color>, moduleElement: ModuleElement, x: Float, y: Float, width: Float, height: Float) : Element(layer, x, y, width, height) {
    val setting: Setting<Color>

    private val expand = Animation(animationSpeed::value, false, easing::value)

    private val hue = Setting("Hue", 0f, 0f, 360f, 1f).setDescription("The hue of the colour")
    private val alpha = Setting("Alpha", 0f, 0f, 255f, 1f).setDescription("The alpha of the colour")
    private val rainbow = Setting("Rainbow", false, false, false, false).setDescription("Whether the colour is a rainbow")
    private val rainbowSpeed = Setting("Speed", 4f, 0f, 10f, 0.1f).setDescription("The speed of the rainbow")
    private val rainbowSaturation = Setting("Saturation", 100f, 0f, 100f, 1f).setDescription("The saturation of the rainbow")
    private val sync = Setting("Sync", false, false, false, false).setDescription("Whether the colour is synced with the client's colour")
    private var finalColour: Color
    private var dragging = false

    init {
        parent = moduleElement.parent
        this.setting = setting
        val hsbColour = Color.RGBtoHSB(setting.value.red, setting.value.green, setting.value.blue, null)
        hue.setValue((hsbColour[0] * 360f).toInt().toFloat())
        alpha.setValue(setting.value.alpha.toFloat())
        rainbow.setValue(setting.isRainbow)
        rainbowSpeed.setValue(setting.rainbowSpeed)
        rainbowSaturation.setValue(setting.rainbowSaturation)
        sync.setValue(setting.isSync)
        val settings: MutableList<Setting<*>> = ArrayList()
        settings.add(hue)
        settings.add(alpha)
        settings.add(rainbow)
        settings.add(rainbowSpeed)
        settings.add(rainbowSaturation)
        settings.add(sync)

        // I hate this btw
        for (setting1 in settings) {
            if (setting1.value is Boolean) {
                subElements.add(BooleanElement(layer + 1, (setting1 as Setting<Boolean?>), moduleElement, x, y, width, height))
            }
            else if (setting1.value is Number) {
                subElements.add(SliderElement(layer + 1, setting1 as Setting<Number?>, moduleElement, x, y, width, height))
            }
        }
        finalColour = setting.value
    }

    override fun render(mouseX: Int, mouseY: Int, dWheel: Int) {
        if (setting.isVisible()) {
            hover.state = isHovered(mouseX, mouseY)
            drawRect(x, y, width, height, Color(40, 40, 45).rgb)
            drawRect(x + layer, y, width - layer * 2, height, Color((40 + 30 * hover.getAnimationFactor()).toInt(), (40 + 30 * hover.getAnimationFactor()).toInt(), (45 + 30 * hover.getAnimationFactor()).toInt()).rgb)
            drawRect(x + layer, y, MathHelper.clamp((width - layer * 2) * expand.getAnimationFactor(), 1.0, width.toDouble()).toFloat(), height, setting.value.rgb)
            drawStringWithShadow(setting.name, x + layer * 2 + 5, y + height / 2 - 3.5f, -0x1)

            // ???
            // why doesnt it stop dragging when mouseReleased is called
            if (!Mouse.isButtonDown(0)) {
                dragging = false
            }
            if (expand.getAnimationFactor() > 0) {
                var offset = y + height

                for (subElement in subElements) {
                    subElement.x = x
                    subElement.y = offset
                    subElement.render(mouseX, mouseY, dWheel)
                    offset += subElement.getTotalHeight()
                }

                setting.alpha = alpha.value
                setting.isRainbow = rainbow.value
                setting.rainbowSaturation = rainbowSaturation.value
                setting.rainbowSpeed = rainbowSpeed.value
                setting.isSync = sync.value
                var hue = hue.value
                val x = x + layer + 2
                val y = y + height + getSubElementsHeight() + 3
                val dimension = width - 6
                val colour = Color.getHSBColor(hue / 360, 1f, 1f)

                // GL shit pt 1
                GlStateManager.pushMatrix()
                GlStateManager.disableTexture2D()
                GlStateManager.enableBlend()
                GlStateManager.disableAlpha()
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
                GlStateManager.shadeModel(7425)

                // Get tessellator and buffer builder
                val tessellator = Tessellator.getInstance()
                val bufferbuilder = tessellator.buffer

                // Add positions
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)
                bufferbuilder.pos((x + dimension).toDouble(), y.toDouble(), 0.0).color(colour.red, colour.green, colour.blue, colour.alpha).endVertex()
                bufferbuilder.pos(x.toDouble(), y.toDouble(), 0.0).color(255, 255, 255, 255).endVertex()
                bufferbuilder.pos(x.toDouble(), (y + dimension + 2).toDouble(), 0.0).color(0, 0, 0, 255).endVertex()
                bufferbuilder.pos((x + dimension).toDouble(), (y + dimension + 2).toDouble(), 0.0).color(0, 0, 0, 255).endVertex()

                // Draw rect
                tessellator.draw()

                // GL shit pt 2
                GlStateManager.shadeModel(7424)
                GlStateManager.enableAlpha()
                GlStateManager.enableTexture2D()
                GlStateManager.popMatrix()
                drawBorder(x, y, dimension, dimension + 2, 0.5f, -1)

                // awful thing to check if we are dragging the hue slider
                for (settingComponent in subElements) {
                    if (settingComponent is SliderElement && settingComponent.isDragging) {
                        hue = this.hue.value
                        val hsb2 = Color.RGBtoHSB(finalColour.red, finalColour.green, finalColour.blue, null)
                        finalColour = Color(Color.HSBtoRGB(hue / 360, hsb2[1], hsb2[2]))
                    }

                    // If we are dragging a slider, we don't want to pick a colour
                    if (settingComponent is SliderElement && settingComponent.isDragging) {
                        dragging = false
                    }
                }

                // Check we are dragging
                if (dragging) {
                    val saturation: Float
                    val brightness: Float
                    val satDiff = Math.min(dimension, Math.max(0f, mouseX - x))
                    saturation = if (satDiff == 0f) {
                        0f
                    }
                    else {
                        roundDouble((satDiff / dimension * 100).toDouble(), 0).toFloat()
                    }
                    val brightDiff = Math.min(dimension, Math.max(0f, y + dimension - mouseY))
                    brightness = if (brightDiff == 0f) {
                        0f
                    }
                    else {
                        roundDouble((brightDiff / dimension * 100).toDouble(), 0).toFloat()
                    }
                    finalColour = Color(Color.HSBtoRGB(hue / 360, saturation / 100, brightness / 100))
                }

                // Get final HSB colours
                val finHSB = Color.RGBtoHSB(finalColour.red, finalColour.green, finalColour.blue, null)

                // Picker X and Y
                val pickerX = x + finHSB[1] * dimension
                val pickerY = y + (1 - finHSB[2]) * dimension

                // Draw picker highlight
                drawRect(pickerX - 1.5f, pickerY - 1.5f, 3f, 3f, -1)
                drawRect(pickerX - 1, pickerY - 1, 2f, 2f, finalColour.rgb)
            }
            setting.setValue(finalColour)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, click: Click) {
        if (setting.isVisible()) {
            if (isHovered(mouseX, mouseY) && parent.isElementVisible(this) && click == Click.RIGHT) {
                expand.state = !expand.state
            }

            val x = x + 1
            val y = y + height + getSubElementsHeight()
            val dimension = width - 6

            if (isHovered(x, y, dimension, dimension, mouseX, mouseY)) {
                dragging = true
            }

            if (expand.state) {
                subElements.forEach(Consumer { subelement: Element -> subelement.mouseClicked(mouseX, mouseY, click) })
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, click: Click) {
        if (setting.isVisible()) {
            subElements.forEach(Consumer { subelement: Element -> subelement.mouseReleased(mouseX, mouseY, click) })
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
        return if (setting.isVisible()) (height + (getSubElementsHeight() + 112) * expand.getAnimationFactor()).toFloat() else 0f
    }
}