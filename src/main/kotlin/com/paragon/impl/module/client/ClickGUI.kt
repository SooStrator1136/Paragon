package com.paragon.impl.module.client

import com.paragon.Paragon
import com.paragon.bus.listener.Listener
import com.paragon.impl.event.client.SettingUpdateEvent
import com.paragon.impl.module.Category
import com.paragon.impl.module.IgnoredByNotifications
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Bind
import com.paragon.impl.setting.Setting
import com.paragon.impl.ui.configuration.GuiImplementation
import com.paragon.impl.ui.configuration.discord.GuiDiscord
import com.paragon.impl.ui.configuration.paragon.ParagonGUI
import me.surge.animation.Easing
import org.lwjgl.input.Keyboard

/**
 * @author SooStrator1136, Surge
 */
@IgnoredByNotifications
object ClickGUI : Module("ClickGUI", Category.CLIENT, "The ClickGUI of the client", Bind(Keyboard.KEY_RSHIFT, Bind.Device.KEYBOARD)) {

    @JvmStatic
    val style: Setting<Style> = Setting("Style", Style.PARAGON) describedBy "The style of the ClickGUI"

    // Windows settings
    @JvmStatic
    val gradient = Setting("Gradient", true) describedBy "Whether the windows should have a gradient" subOf style visibleWhen { style.value == Style.WINDOWS_98 }

    // ZeroDay settings
    @JvmStatic
    val gradientBackground = Setting("GradientBackground", true) describedBy "Whether or not to draw the gradient in the background" subOf style visibleWhen { style.value == Style.ZERODAY }

    @JvmStatic
    val icon = Setting("Icon", Icon.BACKGROUND) describedBy "How to draw the icon" subOf style visibleWhen { style.value == Style.ZERODAY || style.value == Style.PARAGON }

    @JvmStatic
    val radius = Setting("Radius", 1f, 1f, 15f, 1f) describedBy "The radius of the panel's corners" subOf style visibleWhen { style.value == Style.ZERODAY}

    // Shared settings
    @JvmStatic
    val animationSpeed = Setting("AnimationSpeed", 200f, 0f, 1000f, 10f) describedBy "How fast animations are" visibleWhen { style.value != Style.PLUGIN }

    @JvmStatic
    val easing = Setting("Easing", Easing.EXPO_IN_OUT) describedBy "The easing of the animations" visibleWhen { style.value != Style.PLUGIN }

    @JvmStatic
    val darkenBackground = Setting("DarkenBackground", true) describedBy "Whether or not to darken the background"

    @JvmStatic
    val pause = Setting("Pause Game", false) describedBy "Pause the game whilst in the GUI"

    @JvmStatic
    val tooltips = Setting("Tooltips", true) describedBy "Render tooltips on the taskbar" visibleWhen { style.value != Style.PLUGIN }

    val blur = Setting("Blur", true) describedBy "Whether the windows have a blur"

    val intensity = Setting("Intensity", 10f, 1f, 20f, 1f) describedBy "The intensity of the blur" subOf blur

    fun getGUI(): GuiImplementation = when (style.value) {
        Style.PARAGON -> ParagonGUI
        Style.WINDOWS_98 -> Paragon.INSTANCE.windows98GUI
        Style.ZERODAY -> Paragon.INSTANCE.zerodayGUI
        Style.DISCORD -> GuiDiscord
        Style.PLUGIN -> Paragon.INSTANCE.pluginGui ?: ParagonGUI
    }

    override fun onEnable() {
        minecraft.displayGuiScreen(Paragon.INSTANCE.configurationGUI)
        toggle()
    }

    enum class Style {
        /**
         * Original Paragon GUI
         */
        PARAGON,

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
        DISCORD,

        /**
         * The current plugin gui
         */
        PLUGIN,
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