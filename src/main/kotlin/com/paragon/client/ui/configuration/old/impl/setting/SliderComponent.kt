package com.paragon.client.ui.configuration.old.impl.setting

import com.paragon.Paragon
import com.paragon.api.event.client.SettingUpdateEvent
import com.paragon.api.setting.Setting
import com.paragon.api.util.calculations.MathsUtil.roundDouble
import com.paragon.api.util.render.RenderUtil.drawRect
import com.paragon.api.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.api.util.render.font.FontUtil.getStringWidth
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.old.impl.module.ModuleButton
import net.minecraft.util.text.TextFormatting
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.function.Consumer

class SliderComponent(moduleButton: ModuleButton, setting: Setting<Number?>, offset: Float, height: Float) : SettingComponent<Number?>(moduleButton, setting, offset, height) {
    var isDragging = false
        private set

    override fun renderSetting(mouseX: Int, mouseY: Int) {
        drawRect(moduleButton.panel.x, moduleButton.offset + offset, moduleButton.panel.width, height, if (isMouseOver(mouseX, mouseY)) Color(23, 23, 23).brighter().rgb else Color(23, 23, 23).rgb)
        var renderWidth = 0f
        if (setting.value is Float) {
            // Set values
            val diff = Math.min(88f, Math.max(0f, mouseX - (moduleButton.panel.x + 6)))
            val min = setting.min!!.toFloat()
            val max = setting.max!!.toFloat()
            renderWidth = 88 * (setting.value!!.toFloat() - min) / (max - min)
            if (!Mouse.isButtonDown(0)) {
                isDragging = false
            }
            if (isDragging) {
                if (diff == 0f) {
                    setting.setValue(setting.min)
                }
                else {
                    var newValue = roundDouble((diff / 88 * (max - min) + min).toDouble(), 2).toFloat()
                    val precision = 1 / setting.incrementation!!.toFloat()
                    newValue = Math.round(Math.max(min, Math.min(max, newValue)) * precision) / precision
                    setting.setValue(newValue)
                }
            }
        }
        else if (setting.value is Double) {
            // Set values
            val diff = Math.min(88f, Math.max(0f, mouseX - (moduleButton.panel.x + 6))).toDouble()
            val min = setting.min!!.toDouble()
            val max = setting.max!!.toDouble()
            renderWidth = (88 * (setting.value!!.toDouble() - min) / (max - min)).toFloat()
            if (!Mouse.isButtonDown(0)) {
                isDragging = false
            }
            if (isDragging) {
                if (diff == 0.0) {
                    setting.setValue(setting.min)
                }
                else {
                    var newValue = roundDouble(diff / 88 * (max - min) + min, 2)
                    val precision = (1 / setting.incrementation!!.toFloat()).toDouble()
                    newValue = Math.round(Math.max(min, Math.min(max, newValue)) * precision) / precision
                    setting.setValue(newValue)
                }
            }
        }
        GL11.glPushMatrix()
        GL11.glScalef(0.65f, 0.65f, 0.65f)
        run {
            val scaleFactor = 1 / 0.65f
            drawStringWithShadow(setting.name, (moduleButton.panel.x + 5) * scaleFactor, (moduleButton.offset + offset + 3) * scaleFactor, -1)
            val side = (moduleButton.panel.x + moduleButton.panel.width - getStringWidth(setting.value.toString()) * 0.65f - 5) * scaleFactor
            drawStringWithShadow(TextFormatting.GRAY.toString() + " " + setting.value, side, (moduleButton.offset + offset + 3) * scaleFactor, -1)
        }
        GL11.glPopMatrix()
        drawRect(moduleButton.panel.x + 4, moduleButton.offset + offset + 10, 88f, 1f, Color(30, 30, 30).rgb)
        drawRect(moduleButton.panel.x + 4, moduleButton.offset + offset + 10, renderWidth, 1f, Colours.mainColour.value.rgb)
        drawRect(moduleButton.panel.x + 4 + renderWidth - 0.5f, moduleButton.offset + offset + 9.5f, 2f, 2f, -1)
        super.renderSetting(mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0) {
            if (isMouseOver(mouseX, mouseY)) {
                // Set dragging state
                isDragging = true
                val settingUpdateEvent = SettingUpdateEvent(setting)
                Paragon.INSTANCE.eventBus.post(settingUpdateEvent)
            }
        }
        if (isExpanded) {
            settingComponents.forEach(Consumer { settingComponent: SettingComponent<*> ->
                if (settingComponent.setting.isVisible()) {
                    settingComponent.mouseClicked(mouseX, mouseY, mouseButton)
                }
            })
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        isDragging = false
        settingComponents.forEach(Consumer { settingComponent: SettingComponent<*> -> settingComponent.mouseReleased(mouseX, mouseY, mouseButton) })
        super.mouseReleased(mouseX, mouseY, mouseButton)
    }

}