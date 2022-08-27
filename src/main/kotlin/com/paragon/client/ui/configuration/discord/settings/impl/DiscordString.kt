package com.paragon.client.ui.configuration.discord.settings.impl

import com.paragon.Paragon
import com.paragon.api.event.client.SettingUpdateEvent
import com.paragon.api.setting.Setting
import com.paragon.api.util.calculations.Timer
import com.paragon.api.util.render.font.FontUtil
import com.paragon.client.ui.configuration.discord.settings.DiscordSetting
import com.paragon.client.ui.util.Click
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard

/**
 * @author SooStrator1136
 */
class DiscordString(private val setting: Setting<String>) : DiscordSetting(setting) {

    private val cursorTimer = Timer()
    private var typing = false

    init {
        bounds.height = (FontUtil.getHeight() + msgStyleHeight).toInt() + 2
    }

    override fun render(mouseX: Int, mouseY: Int) {
        super.render(mouseX, mouseY)

        FontUtil.drawStringWithShadow(
            setting.value,
            bounds.x.toFloat(),
            bounds.y + FontUtil.getHeight() + 1F,
            -1
        )

        if (typing) {
            if (cursorTimer.hasMSPassed(500.0)) {
                FontUtil.drawStringWithShadow(
                    "|",
                    bounds.x + FontUtil.getStringWidth(setting.value) + 1F,
                    bounds.y + FontUtil.getHeight() + 1F,
                    -1
                )

                if (cursorTimer.hasMSPassed(1000.0)) {
                    cursorTimer.reset()
                }
            }
        }
    }

    override fun onClick(mouseX: Int, mouseY: Int, button: Int) {
        if (!typing && bounds.contains(mouseX, mouseY) && button == Click.LEFT.button) {
            typing = true
        }
    }

    override fun onRelease(mouseX: Int, mouseY: Int, button: Int) {

    }

    override fun onKey(keyCode: Int) {
        if (!typing) {
            return
        }

        val character = Keyboard.getEventCharacter()

        if (keyCode == Keyboard.KEY_BACK) {
            if (setting.value.isNotEmpty()) {
                setting.setValue(setting.value.substring(0, setting.value.length - 1))
                Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
            }
        } else if (keyCode == Keyboard.KEY_RETURN) {
            typing = false
        } else if (ChatAllowedCharacters.isAllowedCharacter(character)) {
            setting.setValue(setting.value + character)
            Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
        }
    }

}