package com.paragon.client.systems.module.hud.impl;

import com.paragon.Paragon;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.systems.module.setting.Setting;
import com.paragon.client.systems.ui.animation.Easing;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class ArrayListHUD extends Module implements TextRenderer {

    public static ArrayListHUD INSTANCE;

    public static Setting<Float> animationSpeed = new Setting<>("Animation", 200f, 0f, 1000f, 10f)
            .setDescription("The speed of the animation");

    public static Setting<ArrayListColour> arrayListColour = new Setting<>("Colour", ArrayListColour.RAINBOW_WAVE)
            .setDescription("What colour to render the modules in");

    public static Setting<Easing> easing = new Setting<>("Easing", Easing.EXPO_IN_OUT)
            .setDescription("The easing type of the animation");

    public static Setting<Boolean> background = new Setting<>("Background", false)
            .setDescription("Render a background behind the text");

    public ArrayListHUD() {
        super("ArrayList", Category.HUD, "Renders the enabled modules on screen");

        INSTANCE = this;
    }

    @Override
    public void onRender2D() {
        ScaledResolution sr = new ScaledResolution(mc);

        int index = 0;

        // Creating a new list, so we can sort these, but not the module manager list
        List<Module> modules = new ArrayList<>();
        for (Module module : Paragon.INSTANCE.getModuleManager().getModules()) {
            if (module.animation.getAnimationFactor() > 0 && module.isVisible()) {
                modules.add(module);
            }
        }

        // Sort by module length
        modules.sort(Comparator.comparingDouble(module -> getStringWidth(module.getName() + module.getArrayListInfo())));
        Collections.reverse(modules);

        float y = sr.getScaledHeight() - 11;
        for (Module module : modules) {
            float x = (float) (sr.getScaledWidth() - (getStringWidth(module.getName() + module.getArrayListInfo())) * module.animation.getAnimationFactor());

            if (background.getValue()) {
                RenderUtil.drawRect((float) (x - (2.5f * module.animation.getAnimationFactor())), y, getStringWidth(module.getName() + module.getArrayListInfo()) + 4, 11, 0x90000000);
            }

            renderText(module.getName() + formatCode(TextFormatting.GRAY) + module.getArrayListInfo(), x - 2, y + 1.5f, arrayListColour.getValue().getColour(index * 150));

            y -= 11 * module.animation.getAnimationFactor();
            index++;
        }
    }

    public enum ArrayListColour {
        /**
         * The colour is slightly different for each module in the array list
         */
        RAINBOW_WAVE((addition) -> ColourUtil.getRainbow(Colours.mainColour.getRainbowSpeed(), Colours.mainColour.getRainbowSaturation() / 100f, addition)),

        /**
         * Permanent static colour
         */
        SYNC((addition) -> Colours.mainColour.getValue().getRGB());

        private final Function<Integer, Integer> colour;

        ArrayListColour(Function<Integer, Integer> colour) {
            this.colour = colour;
        }

        /**
         * Gets the colour
         *
         * @param addition The addition to the colour
         * @return The colour
         */
        public int getColour(int addition) {
            return colour.apply(addition);
        }
    }
}
