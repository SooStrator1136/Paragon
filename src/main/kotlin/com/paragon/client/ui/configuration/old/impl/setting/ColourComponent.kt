package com.paragon.client.ui.configuration.old.impl.setting

import com.paragon.Paragon
import com.paragon.api.event.client.SettingUpdateEvent
import com.paragon.api.setting.Setting
import com.paragon.api.util.calculations.MathsUtil.roundDouble
import com.paragon.api.util.render.ColourUtil.integrateAlpha
import com.paragon.api.util.render.RenderUtil.drawBorder
import com.paragon.api.util.render.RenderUtil.drawRect
import com.paragon.api.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.client.ui.configuration.old.OldPanelGUI.Companion.isInside
import com.paragon.client.ui.configuration.old.impl.module.ModuleButton
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.function.Consumer

/**
 * @author Wolfsurge
 */
class ColourComponent(moduleButton: ModuleButton, setting: Setting<Color>, offset: Float, height: Float) : SettingComponent<Color>(moduleButton, setting, offset, height) {
    private val hue: Setting<Float>
    private val alpha: Setting<Float>
    private val rainbow: Setting<Boolean>
    private val rainbowSpeed: Setting<Float>
    private val rainbowSaturation: Setting<Float>
    private val sync: Setting<Boolean>
    private val components: MutableList<SettingComponent<*>> = ArrayList()
    private var finalColour: Color
    private var dragging = false

    init {
        val hsbColour = Color.RGBtoHSB(setting.value.red, setting.value.green, setting.value.blue, null)
        hue = Setting("Hue", (hsbColour[0] * 360f).toInt().toFloat(), 0f, 360f, 1f).setDescription("The hue of the colour")
        alpha = Setting("Alpha", setting.value.alpha.toFloat(), 0f, 255f, 1f).setDescription("The alpha of the colour")
        rainbow = Setting("Rainbow", setting.isRainbow, setting.isRainbow, setting.isRainbow, setting.isRainbow).setDescription("Whether the colour is a rainbow")
        rainbowSpeed = Setting("Rainbow Speed", setting.rainbowSpeed, 0.1f, 10f, 0.1f).setDescription("The speed of the rainbow")
        rainbowSaturation = Setting("Rainbow Saturation", setting.rainbowSaturation, 0f, 100f, 1f).setDescription("The saturation of the rainbow")
        sync = Setting("Sync", setting.isSync, setting.isSync, setting.isSync, setting.isSync).setDescription("Whether the colour is synced to the client's main colour")
        val settings: MutableList<Setting<*>> = ArrayList()
        settings.add(hue)
        settings.add(alpha)
        settings.add(rainbow)
        settings.add(rainbowSpeed)
        settings.add(rainbowSaturation)
        settings.add(sync)

        // I hate this btw
        var count = 2f
        for (setting1 in settings) {
            if (setting1.value is Boolean) {
                components.add(BooleanComponent(moduleButton, setting1 as Setting<Boolean>, offset + height * count, height))
            }
            else if (setting1.value is Float || setting1.value is Double) {
                components.add(SliderComponent(moduleButton, setting1 as Setting<Number?>, offset + height * count, height))
            }
            count++
        }
        finalColour = setting.value
    }

    override fun renderSetting(mouseX: Int, mouseY: Int) {
        drawRect(moduleButton.panel.x, moduleButton.offset + offset, moduleButton.panel.width, height, if (isInside(moduleButton.panel.x, moduleButton.offset + offset, moduleButton.panel.x + moduleButton.panel.width, moduleButton.offset + offset + 13, mouseX, mouseY)) Color(23, 23, 23).brighter().rgb else Color(23, 23, 23).rgb)
        GL11.glPushMatrix()
        GL11.glScalef(0.65f, 0.65f, 0.65f)
        val scaleFactor = 1 / 0.65f
        drawStringWithShadow(setting.name, (moduleButton.panel.x + 5) * scaleFactor, (moduleButton.offset + offset + 4.5f) * scaleFactor, -1)
        GL11.glScalef(scaleFactor, scaleFactor, scaleFactor)
        GL11.glScalef(0.5f, 0.5f, 0.5f)
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("...", (moduleButton.panel.x + moduleButton.panel.width - 6.5f) * 2, (moduleButton.offset + offset + 3.5f) * 2, -1)
        GL11.glPopMatrix()
        drawBorder(moduleButton.panel.x + moduleButton.panel.width - 20, moduleButton.offset + offset + 2.5f, 8f, 8f, 0.5f, -1)
        drawRect(moduleButton.panel.x + moduleButton.panel.width - 20, moduleButton.offset + offset + 2.5f, 8f, 8f, setting.value!!.rgb)
        var off = offset + 13
        for (settingComponent in components) {
            settingComponent.offset = off
            off += 13f
        }

        // ???
        // why doesnt it stop dragging when mouseReleased is called
        if (!Mouse.isButtonDown(0)) {
            dragging = false
        }
        if (isExpanded) {
            // Render sliders
            components.forEach(Consumer { settingComponent: SettingComponent<*> -> settingComponent.renderSetting(mouseX, mouseY) })
            setting.alpha = alpha.value
            setting.isRainbow = rainbow.value
            setting.rainbowSaturation = rainbowSaturation.value
            setting.rainbowSpeed = rainbowSpeed.value
            setting.isSync = sync.value
            var hue = hue.value
            val x = moduleButton.panel.x + 4
            val y = moduleButton.offset + offset + components.size * 13 + 15.5f
            val dimension = 87f
            val height = dimension * animation.getAnimationFactor().toFloat()
            val colour = Color.getHSBColor(hue / 360, 1f, 1f)

            // Background
            drawRect(moduleButton.panel.x, y - 3.5f, moduleButton.panel.width, height + 7.5f, Color(23, 23, 23).rgb)

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
            bufferbuilder.pos(x.toDouble(), (y + height).toDouble(), 0.0).color(0, 0, 0, 255).endVertex()
            bufferbuilder.pos((x + dimension).toDouble(), (y + height).toDouble(), 0.0).color(0, 0, 0, 255).endVertex()

            // Draw rect
            tessellator.draw()

            // GL shit pt 2
            GlStateManager.shadeModel(7424)
            GlStateManager.enableAlpha()
            GlStateManager.enableTexture2D()
            GlStateManager.popMatrix()
            drawBorder(x, y, dimension, height, 0.5f, -1)

            // awful thing to check if we are dragging the hue slider
            for (settingComponent in components) {
                if (settingComponent.setting == this.hue && (settingComponent as SliderComponent).isDragging) {
                    hue = (settingComponent.setting.value as Number).toFloat()
                    val hsb2 = Color.RGBtoHSB(finalColour.red, finalColour.green, finalColour.blue, null)
                    finalColour = Color(Color.HSBtoRGB(hue / 360, hsb2[1], hsb2[2]))
                }

                // If we are dragging a slider, we don't want to pick a colour
                if (settingComponent is SliderComponent && settingComponent.isDragging) {
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
                val brightDiff = Math.min(height, Math.max(0f, y + height - mouseY))
                brightness = if (brightDiff == 0f) {
                    0f
                }
                else {
                    roundDouble((brightDiff / height * 100).toDouble(), 0).toFloat()
                }
                finalColour = Color(Color.HSBtoRGB(hue / 360, saturation / 100, brightness / 100))
            }

            // Get final HSB colours
            val finHSB = Color.RGBtoHSB(finalColour.red, finalColour.green, finalColour.blue, null)

            // Picker X and Y
            val pickerX = x + finHSB[1] * dimension
            val pickerY = y + (1 - finHSB[2]) * height

            // Draw picker highlight
            drawRect(pickerX - 1.5f, pickerY - 1.5f, 3f, 3f, -1)
            drawRect(pickerX - 1, pickerY - 1, 2f, 2f, finalColour.rgb)
        }

        // Set final colour
        setting.setValue(finalColour.integrateAlpha(alpha.value))
        super.renderSetting(mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (isInside(moduleButton.panel.x, moduleButton.offset + offset, moduleButton.panel.x + moduleButton.panel.width, moduleButton.offset + offset + 13, mouseX, mouseY)) {
            // Toggle open state
            animation.state = !isExpanded
        }
        val x = moduleButton.panel.x + 4
        val y = moduleButton.offset + offset + components.size * 13 + 15.5f
        val dimension = 87f
        if (isInside(x, y, x + dimension, y + dimension, mouseX, mouseY)) {
            dragging = true
        }
        if (isExpanded) {
            components.forEach(Consumer { settingComponent: SettingComponent<*> ->
                settingComponent.mouseClicked(mouseX, mouseY, mouseButton)
                val settingUpdateEvent = SettingUpdateEvent(setting)
                Paragon.INSTANCE.eventBus.post(settingUpdateEvent)
            })
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        dragging = false
        if (isExpanded) {
            components.forEach(Consumer { settingComponent: SettingComponent<*> -> settingComponent.mouseReleased(mouseX, mouseY, mouseButton) })
        }
        super.mouseReleased(mouseX, mouseY, mouseButton)
    }

}