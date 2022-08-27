package com.paragon.client.ui.configuration.discord

import com.paragon.Paragon
import com.paragon.api.util.Wrapper
import com.paragon.client.systems.module.impl.client.ClickGUI
import com.paragon.client.ui.configuration.GuiImplementation
import com.paragon.client.ui.configuration.discord.category.CategoryBar
import com.paragon.client.ui.configuration.discord.module.ModuleBar
import com.paragon.client.ui.configuration.discord.settings.SettingsBar
import org.lwjgl.util.Rectangle
import java.awt.Color

/**
 * @author SooStrator1136
 */
object GuiDiscord : GuiImplementation(), Wrapper {

    var D_WHEEL = 0
        private set
    val BASE_RECT = Rectangle()

    val USER_FIELD_BACKGROUND = Color(41, 43, 47)
    val USER_COPIED_COLOR = Color(59, 165, 93)

    val CATEGORY_BAR_BACKGROUND = Color(32, 34, 37)
    val CATEGORY_TEXT_BACKGROUND = Color(24, 25, 28)

    val CHANNEL_BAR_BACKGROUND = Color(47, 49, 54)
    val CHANNEL_TEXT_COLOR = Color(142, 146, 151)
    val CHANNEL_HOVERED_COLOR = Color(60, 63, 69)

    val MEDIA_BACKGROUND_BORDER = Color(44, 46, 51)
    val MEDIA_BACKGROUND = CHANNEL_BAR_BACKGROUND
    val MEDIA_PROGRESS_BACKGROUND = Color(19, 20, 22)
    val MEDIA_PROGRESS = Color(88, 101, 242)
    val MEDIA_PROGRESSBAR_BACKGROUND = Color(69, 70, 73)
    val MEDIA_SIZE = Color(107, 110, 114)
    val MEDIA_TITLE = Color(11, 155, 198)
    val MSG_HOVERED = Color(50, 53, 59)
    val CHAT_BACKGROUND = Color(54, 57, 63)

    private val renderables = arrayOf(
        SettingsBar,
        ModuleBar,
        CategoryBar
    )

    override fun drawScreen(mouseX: Int, mouseY: Int, mouseDelta: Int) {
        BASE_RECT.setBounds(
            ((minecraft.currentScreen ?: return).width / 2) - 200,
            ((minecraft.currentScreen ?: return).height / 2) - 150,
            400,
            300
        )

        D_WHEEL = mouseDelta

        for (renderable in renderables) {
            renderable.render(mouseX, mouseY)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (renderable in renderables) {
            renderable.onClick(mouseX, mouseY, mouseButton)
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (renderable in renderables) {
            renderable.onRelease(mouseX, mouseY, mouseButton)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        for (renderable in renderables) {
            renderable.onKey(keyCode)
        }
    }

    override fun onGuiClosed() {
        ModuleBar.scrollOffset = 0
        ModuleBar.focusedModule = null
        ModuleBar.shownModules.clear()
        ModuleBar.lastCopyTime = 0L
        SettingsBar.shownSettings.clear()
        SettingsBar.scrollOffset = 0
        Paragon.INSTANCE.configurationGUI.closeOnEscape = true

        super.onGuiClosed()
    }

    override fun doesGuiPauseGame() = ClickGUI.pause.value

}