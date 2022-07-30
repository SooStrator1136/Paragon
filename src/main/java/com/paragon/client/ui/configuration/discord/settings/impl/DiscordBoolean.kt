package com.paragon.client.ui.configuration.discord.settings.impl

import com.paragon.api.setting.Setting
import com.paragon.api.util.render.ITextRenderer
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.discord.settings.DiscordSetting
import org.lwjgl.util.Rectangle

/**
 * @author SooStrator1136
 */
class DiscordBoolean(val setting: Setting<Boolean>) : DiscordSetting(setting), ITextRenderer {

    private val stateRect = Rectangle()

    init {
        bounds.height = (fontHeight + msgStyleHeight).toInt()
    }

    override fun render(mouseX: Int, mouseY: Int) {
        super.render(mouseX, mouseY)

        stateRect.setBounds(
            bounds.x,
            (bounds.y + fontHeight + 1).toInt(),
            getStringWidth(setting.value.toString()).toInt(),
            fontHeight.toInt()
        )

        renderText(
            setting.value.toString(),
            bounds.x.toFloat(),
            bounds.y + fontHeight + 1F,
            if (stateRect.contains(mouseX, mouseY)) Colours.mainColour.value.rgb else -1
        )
    }

    override fun onClick(mouseX: Int, mouseY: Int, button: Int) {
        if (button != 0) {
            return
        }
        if (stateRect.contains(mouseX, mouseY)) {
            setting.setValue(!setting.value)
        }
    }

    override fun onKey(keyCode: Int) {}

}