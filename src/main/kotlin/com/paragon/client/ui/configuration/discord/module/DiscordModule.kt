package com.paragon.client.ui.configuration.discord.module

import com.paragon.api.module.Module

import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.font.FontUtil
import com.paragon.client.ui.configuration.discord.GuiDiscord
import com.paragon.client.ui.configuration.discord.IRenderable
import org.lwjgl.util.Rectangle

/**
 * @author SooStrator1136
 */
class DiscordModule(val module: Module) : IRenderable {

    val rect = Rectangle()

    override fun render(mouseX: Int, mouseY: Int) {
        val isHovered = ModuleBar.rect.contains(mouseX, mouseY) && rect.contains(mouseX, mouseY)
        if (isHovered || module == ModuleBar.focusedModule) {
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

        FontUtil.drawStringWithShadow(
            "# ${module.name}",
            rect.x + 5F,
            rect.y + (rect.height / 2F) - (FontUtil.getHeight() / 2),
            GuiDiscord.CHANNEL_TEXT_COLOR.rgb
        )

        //Tooltip
        if (isHovered) {
            RenderUtil.drawRoundedRect(
                mouseX - 4.0,
                (mouseY - (FontUtil.getHeight() / 2.0)) - 1.0,
                FontUtil.getStringWidth(module.description) + 8.0,
                FontUtil.getHeight() + 2.0,
                5.0,
                5.0,
                5.0,
                5.0,
                GuiDiscord.CHANNEL_HOVERED_COLOR.rgb
            )
            RenderUtil.drawRoundedOutline(
                mouseX - 3.0,
                (mouseY - (FontUtil.getHeight() / 2.0)) - 2.0,
                FontUtil.getStringWidth(module.description) + 6.0,
                FontUtil.getHeight() + 4.0,
                5.0,
                5.0,
                5.0,
                5.0,
                1F,
                GuiDiscord.CATEGORY_BAR_BACKGROUND.rgb
            )

            FontUtil.drawStringWithShadow(module.description, mouseX.toFloat(), mouseY - (FontUtil.getHeight() / 2F), -1)
        }
    }

    override fun onClick(mouseX: Int, mouseY: Int, button: Int) {}
    override fun onRelease(mouseX: Int, mouseY: Int, button: Int) {}
    override fun onKey(keyCode: Int) {}

}