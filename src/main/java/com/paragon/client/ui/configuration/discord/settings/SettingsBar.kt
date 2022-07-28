package com.paragon.client.ui.configuration.discord.settings

import com.paragon.api.util.render.RenderUtil
import com.paragon.client.ui.configuration.discord.GuiDiscord
import com.paragon.client.ui.configuration.discord.IRenderable
import com.paragon.client.ui.configuration.discord.category.CategoryBar
import com.paragon.client.ui.configuration.discord.module.ModuleBar
import org.lwjgl.util.Rectangle

/**
 * @author SooStrator1136
 */
object SettingsBar : IRenderable {

    private const val settingOffset = 10
    val shownSettings: MutableList<DiscordSetting> = ArrayList()
    val rect = Rectangle()

    override fun render(mouseX: Int, mouseY: Int) {
        //Set basic bounds
        run {
            rect.setBounds(
                ModuleBar.rect.x + ModuleBar.rect.width,
                ModuleBar.rect.y,
                GuiDiscord.BASE_RECT.width - (CategoryBar.rect.width + ModuleBar.rect.width),
                GuiDiscord.BASE_RECT.height
            )
        }

        RenderUtil.drawRect(
            rect.x.toFloat(),
            rect.y.toFloat(),
            rect.width.toFloat(),
            rect.height.toFloat(),
            GuiDiscord.CHAT_BACKGROUND.rgb
        )

        //Render settings
        run {
            var currY = rect.y + 10
            shownSettings.forEach { setting ->
                setting.bounds.setBounds(rect.x + 15, currY, rect.width - 30, setting.bounds.height)
                currY += setting.bounds.height + settingOffset
            }

            RenderUtil.pushScissor(
                rect.x.toDouble(),
                rect.y.toDouble(),
                rect.width.toDouble(),
                rect.height.toDouble()
            )

            shownSettings.forEach {
                it.render(mouseX, mouseY)
            }

            RenderUtil.popScissor()
        }
    }

    override fun onClick(mouseX: Int, mouseY: Int, button: Int) {

    }

    override fun onKey(keyCode: Int) {

    }

}