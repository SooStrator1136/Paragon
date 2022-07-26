package com.paragon.client.systems.module.impl.client

import com.paragon.Paragon
import com.paragon.api.module.Category
import com.paragon.api.module.IgnoredByNotifications
import com.paragon.api.module.Module
import com.paragon.api.setting.Bind
import com.paragon.api.setting.Setting
import com.paragon.client.ui.util.animation.Easing
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard
import com.paragon.client.ui.configuration.window.WindowGUI

/**
 * @author SooStrator1136
 */
@IgnoredByNotifications
object ClickGUI : Module("ClickGUI", Category.CLIENT, "The ClickGUI of the client", Bind(Keyboard.KEY_RSHIFT, Bind.Device.KEYBOARD)) {

    @JvmStatic
    val style: Setting<Style> = Setting("Style", Style.PANEL)
        .setDescription("The style of the ClickGUI")

    // Panel settings
    @JvmStatic
    val background = Setting("Background", true)
        .setDescription("Whether or not to draw the background")
        .setParentSetting(style)
        .setVisibility { style.value == Style.PANEL }

    @JvmStatic
    val animationSpeed = Setting("AnimationSpeed", 200f, 0f, 1000f, 10f)
        .setDescription("How fast animations are")
        .setParentSetting(style)
        .setVisibility { style.value == Style.PANEL }

    @JvmStatic
    val easing = Setting("Easing", Easing.EXPO_IN_OUT)
        .setDescription("The easing type of the animation")
        .setParentSetting(style)
        .setVisibility { style.value == Style.PANEL }

    @JvmStatic
    val icon = Setting("Icon", Icon.BACKGROUND)
        .setDescription("How to draw the background")
        .setParentSetting(style)
        .setVisibility { style.value == Style.PANEL }

    // Window settings
   /* @JvmStatic
    val scrollClamp = Setting("ScrollClamp", false)
        .setDescription("Clamp scrolling (disable to allow scrolling past the end of the list)")
        .setParentSetting(style)
        .setVisibility { style.value == Style.WINDOW }*/

    // Shared settings
    @JvmStatic
    val radius = Setting("Radius", 1f, 1f, 15f, 1f)
        .setDescription("The radius of the panel's corners")
        .setParentSetting(style)

    @JvmStatic
    val darkenBackground = Setting("DarkenBackground", true)
        .setDescription("Darkens the background whilst in the GUI")

    @JvmStatic
    val pause = Setting("Pause Game", false)
        .setDescription("Pause the game whilst in the GUI")

    @JvmStatic
    val tooltips = Setting("Tooltips", true)
        .setDescription("Render tooltips on the taskbar")

    @JvmStatic
    fun getGUI(): GuiScreen = when (style.value) {
        Style.WINDOW -> WindowGUI()
        Style.PANEL -> Paragon.INSTANCE.panelGUI
    }

    override fun onEnable() {
        minecraft.displayGuiScreen(getGUI())
        toggle()
    }

    enum class Style {
        /**
         * Panel GUI
         */
        PANEL,

        /**
         * Window GUI
         */
        WINDOW
    }

    enum class Icon {
        /**
         * No icon
         */
        NONE,

        /**
         * Just the icon
         */
        PLAIN,

        /**
         * Icon with a background
         */
        BACKGROUND
    }

}