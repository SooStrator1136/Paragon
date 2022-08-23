package com.paragon.client.ui.configuration.old.impl.setting

import com.paragon.Paragon
import com.paragon.api.event.client.SettingUpdateEvent
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.RenderUtil.drawRect
import com.paragon.api.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.api.util.render.font.FontUtil.getStringWidth
import com.paragon.api.util.string.StringUtil.getFormattedText
import com.paragon.client.ui.configuration.old.impl.module.ModuleButton
import net.minecraft.util.text.TextFormatting
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.function.Consumer

class ModeComponent(moduleButton: ModuleButton, setting: Setting<Enum<*>>, offset: Float, height: Float) : SettingComponent<Enum<*>>(moduleButton, setting, offset, height) {
    override fun renderSetting(mouseX: Int, mouseY: Int) {
        drawRect(moduleButton.panel.x, moduleButton.offset + offset, moduleButton.panel.width, height, if (isMouseOver(mouseX, mouseY)) Color(23, 23, 23).brighter().rgb else Color(23, 23, 23).rgb)
        val mode = getFormattedText(setting.value!!)
        GL11.glPushMatrix()
        GL11.glScalef(0.65f, 0.65f, 0.65f)
        run {
            val scaleFactor = 1 / 0.65f
            drawStringWithShadow(setting.name, (moduleButton.panel.x + 5) * scaleFactor, (moduleButton.offset + offset + 4.5f) * scaleFactor, -1)
            val side = (moduleButton.panel.x + moduleButton.panel.width - getStringWidth(mode) * 0.65f - 5) * scaleFactor
            drawStringWithShadow(TextFormatting.GRAY.toString() + " " + mode, side, (moduleButton.offset + offset + 4.5f) * scaleFactor, -1)
        }
        GL11.glPopMatrix()
        super.renderSetting(mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0) {
            if (isMouseOver(mouseX, mouseY)) {
                // Cycle mode
                setting.setValue(setting.nextMode)
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

}