package com.paragon.client.systems.module.impl.client

import com.paragon.Paragon
import com.paragon.api.module.Category
import com.paragon.api.module.IgnoredByNotifications
import com.paragon.api.module.Module
import com.paragon.api.setting.Bind
import com.paragon.api.setting.Setting
import com.paragon.client.ui.configuration.GuiImplementation
import com.paragon.client.ui.configuration.discord.GuiDiscord
import com.paragon.client.ui.util.animation.Easing
import org.lwjgl.input.Keyboard

/**
 * @author SooStrator1136, Surge
 */
@IgnoredByNotifications
object ClickGUI : Module("ClickGUI", Category.CLIENT, "The ClickGUI of the client", Bind(Keyboard.KEY_RSHIFT, Bind.Device.KEYBOARD)) {

    @JvmStatic
    val style: Setting<Style> = Setting("Style", Style.ZERODAY)
        .setDescription("The style of the ClickGUI")

    // Windows settings
    @JvmStatic
    val gradient = Setting("Gradient", true)
        .setDescription("Whether the windows should have a gradient")
        .setParentSetting(style)
        .setVisibility { style.value == Style.WINDOWS_98 }

    // ZeroDay settings
    @JvmStatic
    val gradientBackground = Setting("GradientBackground", true)
        .setDescription("Whether or not to draw the gradient in the background")
        .setParentSetting(style)
        .setVisibility { style.value == Style.ZERODAY }

    @JvmStatic
    val icon = Setting("Icon", Icon.BACKGROUND)
        .setDescription("How to draw the icon")
        .setParentSetting(style)
        .setVisibility { style.value == Style.ZERODAY }

    @JvmStatic
    val radius = Setting("Radius", 1f, 1f, 15f, 1f)
        .setDescription("The radius of the panel's corners")
        .setParentSetting(style)
        .setVisibility { style.value == Style.ZERODAY }

    // Shared settings
    @JvmStatic
    val animationSpeed = Setting("AnimationSpeed", 200f, 0f, 1000f, 10f)
        .setDescription("How fast animations are")

    @JvmStatic
    val easing = Setting("Easing", Easing.EXPO_IN_OUT)
        .setDescription("The easing type of the animation")

    @JvmStatic
    val darkenBackground = Setting("DarkenBackground", true)
        .setDescription("Darkens the background whilst in the GUI")

    @JvmStatic
    val pause = Setting("Pause Game", false)
        .setDescription("Pause the game whilst in the GUI")

    @JvmStatic
    val tooltips = Setting("Tooltips", true)
        .setDescription("Render tooltips on the taskbar")

    val blur = Setting("Blur", true)
        .setDescription("Blur the backgrounds of windows")

    val intensity = Setting("Intensity", 10f, 1f, 20f, 1f)
        .setDescription("The intensity of the blur")
        .setParentSetting(blur)


    fun getGUI(): GuiImplementation = when (style.value) {
        Style.WINDOWS_98 -> Paragon.INSTANCE.windows98GUI
        Style.ZERODAY -> Paragon.INSTANCE.zerodayGUI
        Style.DISCORD -> GuiDiscord
    }

    override fun onEnable() {
        minecraft.displayGuiScreen(Paragon.INSTANCE.configurationGUI)
        toggle()
    }

    enum class Style {
        /**
         * Windows 98 themed GUI
         */
        WINDOWS_98,

        /**
         * AWFUL remake of the ZeroDay b21/22 GUI
         */
        ZERODAY,

        /**
         * Discord like gui
         */
        DISCORD
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