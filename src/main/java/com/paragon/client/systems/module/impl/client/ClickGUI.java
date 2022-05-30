package com.paragon.client.systems.module.impl.client;

import com.paragon.Paragon;
import com.paragon.client.systems.module.IgnoredByNotifications;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;
import com.paragon.client.systems.ui.animation.Easing;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

@IgnoredByNotifications
public class ClickGUI extends Module {

    public static Setting<Style> style = new Setting<>("Style", Style.PANEL)
            .setDescription("The style of the ClickGUI");

    // Panel settings
    public static Setting<Float> scrollSpeed = new Setting<>("Scroll Speed", 10f, 5f, 30f, 1f)
            .setDescription("How fast to scroll")
            .setParentSetting(style)
            .setVisibility(() -> style.getValue().equals(Style.PANEL));

    public static Setting<Boolean> tooltips = new Setting<>("Tooltips", true)
            .setDescription("Render tooltips near the mouse when hovered over a button")
            .setParentSetting(style)
            .setVisibility(() -> style.getValue().equals(Style.PANEL));

    public static Setting<Boolean> panelHeaderSeparator = new Setting<>("Header Separator", true)
            .setDescription("Draw a separator between the header and the module buttons")
            .setParentSetting(style)
            .setVisibility(() -> style.getValue().equals(Style.PANEL));

    public static Setting<AnimationType> animation = new Setting<>("Animation", AnimationType.STATIC)
            .setDescription("The type of animation")
            .setParentSetting(style)
            .setVisibility(() -> style.getValue().equals(Style.PANEL));

    public static Setting<Float> cornerRadius = new Setting<>("Corner Radius", 1f, 1f, 7f, 1f)
            .setDescription("The radius of the corners")
            .setParentSetting(style)
            .setVisibility(() -> style.getValue().equals(Style.PANEL));

    public static Setting<Float> animationSpeed = new Setting<>("Animation Speed", 200f, 0f, 1000f, 10f)
            .setDescription("How fast animations are")
            .setParentSetting(style)
            .setVisibility(() -> style.getValue().equals(Style.PANEL));

    public static Setting<Easing> easing = new Setting<>("Easing", Easing.EXPO_IN_OUT)
            .setDescription("The easing type of the animation")
            .setParentSetting(style)
            .setVisibility(() -> style.getValue().equals(Style.PANEL));

    // Window settings
    public static Setting<Boolean> scrollClamp = new Setting<>("Scroll Clamp", false)
            .setDescription("Clamp scrolling (disable to allow scrolling past the end of the list)")
            .setParentSetting(style)
            .setVisibility(() -> style.getValue().equals(Style.WINDOW));

    // Shared settings
    public static Setting<Boolean> darkenBackground = new Setting<>("Darken Background", true)
            .setDescription("Darkens the background whilst in the GUI");

    public static Setting<Boolean> pause = new Setting<>("Pause Game", false)
            .setDescription("Pause the game whilst in the GUI");

    public ClickGUI() {
        super("ClickGUI", Category.CLIENT, "The ClickGUI of the client", Keyboard.KEY_RSHIFT);
    }

    /**
     * Gets the GUI to switch to
     *
     * @return The GUI to switch to
     */
    public static GuiScreen getGUI() {
        switch (style.getValue()) {
            case WINDOW:
                return Paragon.INSTANCE.getWindowGUI();

            case PANEL:
                return Paragon.INSTANCE.getPanelGUI();
        }

        return Paragon.INSTANCE.getPanelGUI();
    }

    @Override
    public void onEnable() {
        mc.displayGuiScreen(getGUI());
        toggle();
    }

    public enum Style {
        /**
         * Panel GUI
         */
        PANEL,

        /**
         * Window GUI
         */
        WINDOW
    }

    public enum AnimationType {
        /**
         * Leave the components in the same place
         */
        STATIC,

        /**
         * Move the components (like Momentum)
         */
        FACTOR;

        public float getAnimationFactor(float animationIn) {
            if (animation.getValue().equals(STATIC)) {
                return 1;
            } else {
                return animationIn;
            }
        }
    }
}

