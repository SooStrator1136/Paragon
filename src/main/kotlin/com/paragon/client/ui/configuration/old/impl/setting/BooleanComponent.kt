package com.paragon.client.ui.configuration.old.impl.setting

import com.paragon.Paragon
import com.paragon.api.event.client.SettingUpdateEvent
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.RenderUtil.drawRect
import com.paragon.api.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.old.impl.module.ModuleButton
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.function.Consumer

class BooleanComponent(moduleButton: ModuleButton, setting: Setting<Boolean>, offset: Float, height: Float) : SettingComponent<Boolean>(moduleButton, setting, offset, height) {
    override fun renderSetting(mouseX: Int, mouseY: Int) {
        // Background
        drawRect(moduleButton.panel.x, moduleButton.offset + offset, moduleButton.panel.width, height, if (isMouseOver(mouseX, mouseY)) Color(23, 23, 23).brighter().rgb else Color(23, 23, 23).rgb)

        // Render setting name
        GL11.glPushMatrix()
        GL11.glScalef(0.65f, 0.65f, 0.65f)
        val scaleFactor = 1 / 0.65f
        drawStringWithShadow(setting.name, (moduleButton.panel.x + 5) * scaleFactor, (moduleButton.offset + offset + 4.5f) * scaleFactor, if (setting.value!!) Colours.mainColour.value.rgb else -1)
        GL11.glPopMatrix()
        super.renderSetting(mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0) {
            if (isMouseOver(mouseX, mouseY)) {
                // Toggle setting
                setting.setValue(!setting.value!!)
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