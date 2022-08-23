package com.paragon.client.ui.configuration.old.impl.setting

import com.paragon.Paragon
import com.paragon.api.event.client.SettingUpdateEvent
import com.paragon.api.setting.Bind
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.RenderUtil.drawRect
import com.paragon.api.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.api.util.render.font.FontUtil.getStringWidth
import com.paragon.client.ui.configuration.old.impl.module.ModuleButton
import net.minecraft.util.text.TextFormatting
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.function.Consumer

class KeybindComponent(moduleButton: ModuleButton, setting: Setting<Bind?>, offset: Float, height: Float) : SettingComponent<Bind?>(moduleButton, setting, offset, height) {
    private var isListening = false
    override fun renderSetting(mouseX: Int, mouseY: Int) {
        drawRect(moduleButton.panel.x, moduleButton.offset + offset, moduleButton.panel.width, height, if (isMouseOver(mouseX, mouseY)) Color(23, 23, 23).brighter().rgb else Color(23, 23, 23).rgb)
        GL11.glPushMatrix()
        GL11.glScalef(0.65f, 0.65f, 0.65f)
        run {
            val scaleFactor = 1 / 0.65f
            drawStringWithShadow(setting.name, (moduleButton.panel.x + 5) * scaleFactor, (moduleButton.offset + offset + 4.5f) * scaleFactor, -1)
            val side = (moduleButton.panel.x + moduleButton.panel.width - getStringWidth(if (isListening) " ..." else " " + setting.value!!.getButtonName()) * 0.65f - 5) * scaleFactor
            drawStringWithShadow(TextFormatting.GRAY.toString() + if (isListening) " ..." else " " + setting.value!!.getButtonName(), side, (moduleButton.offset + offset + 4.5f) * scaleFactor, -1)
        }
        GL11.glPopMatrix()
        super.renderSetting(mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (isListening) {
            isListening = false
            setting.value!!.device = Bind.Device.MOUSE
            setting.value!!.buttonCode = mouseButton
            return
        }
        if (isMouseOver(mouseX, mouseY) && mouseButton == 0) {
            // Set listening
            isListening = !isListening
            val settingUpdateEvent = SettingUpdateEvent(setting)
            Paragon.INSTANCE.eventBus.post(settingUpdateEvent)
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

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (isListening) {
            isListening = false
            setting.value!!.device = Bind.Device.KEYBOARD
            if (keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK) {
                setting.value!!.buttonCode = 0
                return
            }
            setting.value!!.buttonCode = keyCode
        }
        if (isExpanded) {
            settingComponents.forEach(Consumer { settingComponent: SettingComponent<*> -> settingComponent.keyTyped(typedChar, keyCode) })
        }
    }
}