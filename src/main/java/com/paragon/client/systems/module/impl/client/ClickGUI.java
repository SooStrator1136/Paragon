package com.paragon.client.systems.module.impl.client;

import com.paragon.Paragon;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;
import com.paragon.client.systems.ui.animation.Animation;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

public class ClickGUI extends Module {

    // Panel settings
    public static Setting<Float> scrollSpeed = new Setting<>("Scroll Speed", 10f, 5f, 30f, 1f)
            .setDescription("How fast to scroll");

    public static Setting<Boolean> tooltips = new Setting<>("Tooltips", true)
            .setDescription("Render tooltips near the mouse when hovered over a button");


    public static Setting<Boolean> panelHeaderSeparator = new Setting<>("Header Separator", false)
            .setDescription("Draw a separator between the header and the module buttons");

    public static Setting<AnimationType> animation = new Setting<>("Animation", AnimationType.STATIC)
            .setDescription("The type of animation");

    public static Setting<Float> cornerRadius = new Setting<>("Corner Radius", 5f, 1f, 7f, 1f)
            .setDescription("The radius of the corners");


    public static Setting<Float> animationSpeed = new Setting<>("Animation Speed", 200f, 0f, 1000f, 10f)
            .setDescription("How fast animations are");

    public static Setting<Animation.Easing> easing = new Setting<>("Easing", Animation.Easing.LINEAR)
            .setDescription("The easing type of the animation");

    // Shared settings
    public static Setting<Boolean> darkenBackground = new Setting<>("Darken Background", true)
            .setDescription("Darkens the background whilst in the GUI");

    public static Setting<Boolean> pause = new Setting<>("Pause Game", false)
            .setDescription("Pause the game whilst in the GUI");

    public ClickGUI() {
        super("ClickGUI", Category.CLIENT, "The ClickGUI of the client", Keyboard.KEY_RSHIFT);
        this.addSettings(scrollSpeed, tooltips, panelHeaderSeparator, animation, cornerRadius, animationSpeed, easing, darkenBackground, pause);
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
        // return new WindowGUI();

        return Paragon.INSTANCE.getPanelGUI();
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

