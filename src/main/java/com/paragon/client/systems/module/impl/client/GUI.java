package com.paragon.client.systems.module.impl.client;

import com.paragon.Paragon;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

@SuppressWarnings("unchecked")
public class GUI extends Module {

    // GUI style
    public static ModeSetting<Style> style = new ModeSetting<>("Style", "The style of the GUI", Style.PANEL);

    // Window settings
    public static BooleanSetting settingOutline = (BooleanSetting) new BooleanSetting("Setting Outline", "Draws an outline around the settings and expanded module", false).setVisiblity(() -> style.getCurrentMode() == Style.WINDOW).setParentSetting(style);
    public static BooleanSetting windowOutline = (BooleanSetting) new BooleanSetting("Window Outline", "Draws an outline around the window", true).setVisiblity(() -> style.getCurrentMode() == Style.WINDOW).setParentSetting(style);
    public static BooleanSetting separator = (BooleanSetting) new BooleanSetting("Separator", "Draws a bar in between the category buttons and the modules", true).setVisiblity(() -> style.getCurrentMode() == Style.WINDOW).setParentSetting(style);

    // Panel settings
    public static NumberSetting scrollSpeed = (NumberSetting) new NumberSetting("Scroll Speed", "How fast to scroll", 10, 5, 30, 1).setVisiblity(() -> style.getCurrentMode() == Style.PANEL).setParentSetting(style);
    public static BooleanSetting tooltips = (BooleanSetting) new BooleanSetting("Tooltips", "Render tooltips near the mouse when hovered over a button", true).setVisiblity(() -> style.getCurrentMode() == Style.PANEL).setParentSetting(style);
    public static BooleanSetting outline = (BooleanSetting) new BooleanSetting("Outline", "Outlines the panel", true).setParentSetting(style).setVisiblity(() -> style.getCurrentMode() == Style.PANEL);
    public static BooleanSetting panelHeaderSeparator = (BooleanSetting) new BooleanSetting("Header Separator", "Draw a separator between the header and the module buttons", false).setParentSetting(style).setVisiblity(() -> style.getCurrentMode() == Style.PANEL);
    public static ModeSetting<Animation> animation = (ModeSetting<Animation>) new ModeSetting<>("Animation", "The type of animation", Animation.STATIC).setParentSetting(style).setVisiblity(() -> style.getCurrentMode() == Style.PANEL);

    public static NumberSetting animationSpeed = new NumberSetting("Animation Speed", "How fast animations are", 100, 0, 1000, 10);

    // Shared settings
    public static BooleanSetting darkenBackground = new BooleanSetting("Darken Background", "Darkens the background whilst in the GUI", true);
    public static BooleanSetting pause = new BooleanSetting("Pause Game", "Pause the game whilst in the GUI", false);

    public GUI() {
        super("GUI", ModuleCategory.CLIENT, "The GUI of the client", Keyboard.KEY_RSHIFT);
        this.addSettings(style, animationSpeed, darkenBackground, pause);
    }

    @Override
    public void onEnable() {
        mc.displayGuiScreen(getGUI());
        toggle();
    }

    /**
     * Gets the GUI to switch to
     * @return The GUI to switch to
     */
    public static GuiScreen getGUI() {
        switch (style.getCurrentMode()) {
            case PANEL:
                return Paragon.INSTANCE.getPanelGUI();
            case WINDOW:
                return Paragon.INSTANCE.getWindowGUI();
        }

        return Paragon.INSTANCE.getPanelGUI();
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

    public enum Animation {
        /**
         * Leave the components in the same place
         */
        STATIC,

        /**
         * Move the components (like Momentum)
         */
        FACTOR;

        public float getAnimationFactor(float animationIn) {
            if (animation.getCurrentMode() == STATIC) {
                return 1;
            } else {
                return animationIn;
            }
        }
    }
}

