package com.paragon.client.ui.configuration.discord.module

import com.paragon.api.module.Module
import com.paragon.api.util.render.ITextRenderer
import com.paragon.api.util.render.RenderUtil
import com.paragon.client.ui.configuration.discord.GuiDiscord
import com.paragon.client.ui.configuration.discord.IRenderable
import org.lwjgl.util.Rectangle

/**
 * @author SooStrator1136
 */
class DiscordModule(val module: Module) : IRenderable, ITextRenderer {

    var isFocused = false
    val rect = Rectangle()

    override fun render(mouseX: Int, mouseY: Int) {
        if (ModuleBar.rect.contains(mouseX, mouseY) && rect.contains(mouseX, mouseY) || isFocused) {
            RenderUtil.drawRoundedRect(
                rect.x.toDouble(),
                rect.y.toDouble(),
                rect.width.toDouble(),
                rect.height.toDouble(),
                10.0,
                10.0,
                10.0,
                10.0,
                GuiDiscord.CHANNEL_HOVERED_COLOR.rgb
            )
        }

        renderText(
            "# ${module.name}",
            rect.x + 5F,
            rect.y + (rect.height / 2F) - (fontHeight / 2),
            GuiDiscord.CHANNEL_TEXT_COLOR.rgb
        )
    }

    override fun onClick(mouseX: Int, mouseY: Int, button: Int) {}
    override fun onKey(keyCode: Int) {}

}