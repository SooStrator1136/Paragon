package com.paragon.client.ui.configuration.discord.settings.impl

import com.paragon.api.setting.Setting
import com.paragon.api.util.render.ITextRenderer
import com.paragon.client.ui.configuration.discord.settings.DiscordSetting
import java.awt.Color

/**
 * @author SooStrator1136
 */
class DiscordBoolean(val setting: Setting<Boolean>) : DiscordSetting(setting), ITextRenderer {

    init {
        bounds.setBounds(
            0,
            0,
            getStringWidth(setting.name + " = " + setting.value).toInt(),
            (fontHeight + msgStyleHeight).toInt()
        )
    }

    //Will fix and actually unshit the look at some point

    override fun render(mouseX: Int, mouseY: Int) {
        super.render(mouseX, mouseY)

        renderText(setting.value.toString(), bounds.x.toFloat(), bounds.y + fontHeight + 1F, Color.WHITE.rgb)
    }

    override fun onClick(mouseX: Int, mouseY: Int, button: Int) {
        if (button == 0 && bounds.contains(mouseX, mouseY)) {
            setting.setValue(!setting.value)
        }
    }

    override fun onKey(keyCode: Int) {}

}