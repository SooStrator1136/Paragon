package com.paragon.client.ui.configuration.discord.settings.impl

import com.paragon.api.setting.Setting
import com.paragon.api.util.calculations.Timer
import com.paragon.client.ui.configuration.discord.settings.DiscordSetting
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard

/**
 * @author SooStrator1136
 */
class DiscordString(val setting: Setting<String>) : DiscordSetting(setting) {

    private val cursorTimer = Timer()
    private var typing = false

    init {
        bounds.height = (fontHeight + msgStyleHeight).toInt()
    }

    override fun render(mouseX: Int, mouseY: Int) {
        super.render(mouseX, mouseY)

        renderText(
            setting.value,
            bounds.x.toFloat(),
            bounds.y + fontHeight + 1F,
            -1
        )

        if (typing) {
            if (cursorTimer.hasMSPassed(500.0)) {
                renderText(
                    "|",
                    bounds.x + getStringWidth(setting.value) + 1F,
                    bounds.y + fontHeight + 1F,
                    -1
                )

                if (cursorTimer.hasMSPassed(1000.0)) {
                    cursorTimer.reset()
                }
            }
        }
    }

    override fun onClick(mouseX: Int, mouseY: Int, button: Int) {
        if (!typing) {
            typing = true
        }
    }

    override fun onKey(keyCode: Int) {
        if (!typing) {
            return
        }

        val character = Keyboard.getEventCharacter()

        if (keyCode == Keyboard.KEY_BACK) {
            if (setting.value.isNotEmpty()) {
                setting.setValue(setting.value.substring(0, setting.value.length - 1))
            }
        } else if (keyCode == Keyboard.KEY_RETURN) {
            typing = false
        } else if (ChatAllowedCharacters.isAllowedCharacter(character)) {
            setting.setValue(setting.value + character)
        }
    }

}