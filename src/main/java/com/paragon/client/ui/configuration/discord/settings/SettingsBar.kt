package com.paragon.client.ui.configuration.discord.settings

import com.paragon.api.setting.Bind
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyIndexed
import com.paragon.api.util.render.RenderUtil
import com.paragon.client.ui.configuration.discord.GuiDiscord
import com.paragon.client.ui.configuration.discord.IRenderable
import com.paragon.client.ui.configuration.discord.category.CategoryBar
import com.paragon.client.ui.configuration.discord.module.ModuleBar
import com.paragon.client.ui.configuration.discord.settings.impl.*
import org.lwjgl.util.Rectangle
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author SooStrator1136
 */
object SettingsBar : IRenderable {

    var scrollOffset = 0
    private var maxScrollOffset = 0

    private const val settingOffset = 10

    val shownSettings: MutableList<DiscordSetting> = CopyOnWriteArrayList()
    val rect = Rectangle()

    override fun render(mouseX: Int, mouseY: Int) {
        //Set basic bounds and settings stuff
        run {
            rect.setBounds(
                ModuleBar.rect.x + ModuleBar.rect.width,
                ModuleBar.rect.y,
                GuiDiscord.BASE_RECT.width - (CategoryBar.rect.width + ModuleBar.rect.width),
                GuiDiscord.BASE_RECT.height
            )

            if (rect.contains(mouseX, mouseY) && shownSettings.isNotEmpty()) {
                val lastRect = shownSettings[shownSettings.size - 1].bounds
                maxScrollOffset = Optional.of(
                    ((((lastRect.y + lastRect.height) - shownSettings[0].bounds.y) - rect.height) * -1) - 25
                ).map { if (it > 0) 0 else it }.get()
                val newOffset = scrollOffset + (GuiDiscord.D_WHEEL / 7)
                if (GuiDiscord.D_WHEEL < 0) {
                    scrollOffset = if (newOffset < maxScrollOffset) maxScrollOffset else newOffset
                } else if (scrollOffset < 0) {
                    scrollOffset = if (newOffset > 0) 0 else newOffset
                }
            }

            ModuleBar.focusedModule?.settings?.forEach {
                addSetting(it)
                addSubSettings(it.subsettings)
            }
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
            var currY = rect.y + scrollOffset + 10
            shownSettings.forEach { setting ->
                setting.bounds.setBounds(rect.x + 15, currY, rect.width - 30, setting.bounds.height)
                currY += setting.bounds.height + settingOffset
            }

            RenderUtil.pushScissor(
                rect.x.toDouble(),
                rect.y + 1.0,
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
        if (!rect.contains(mouseX, mouseY)) {
            return
        }
        shownSettings.forEach {
            it.onClick(mouseX, mouseY, button)
        }
    }

    override fun onKey(keyCode: Int) {
        shownSettings.forEach {
            it.onKey(keyCode)
        }
    }

    //Adding subsettings as normal setting because Idk how subsettings in a chat type thing would work
    private fun addSubSettings(subSettings: ArrayList<Setting<*>>) {
        if (subSettings.isEmpty()) {
            return
        }
        subSettings.forEach {
            addSetting(it)
            addSubSettings(it.subsettings)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun addSetting(setting: Setting<*>) {
        if (!setting.isVisible() || shownSettings.any { it.dSetting == setting }) {
            return
        }
        val toAdd = when (setting.value) {
            is Boolean -> DiscordBoolean(setting as Setting<Boolean>)
            is Enum<*> -> DiscordEnum(setting as Setting<Enum<*>>)
            is String -> DiscordString(setting as Setting<String>)
            is Bind -> DiscordBind(setting as Setting<Bind>)
            is Number -> DiscordNumber(setting as Setting<Number>)
            else -> null //Should be unreachable when all setting types are implemented
        } ?: return

        if (setting.parentSetting != null) {
            shownSettings.add(
                shownSettings.anyIndexed { it.dSetting == setting.parentSetting } + 1,
                toAdd
            ) //Add right after index of parentSetting
        } else {
            shownSettings.add(toAdd)
        }
    }

}