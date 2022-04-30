package com.paragon.client.systems.module.hud.impl;

import com.paragon.Paragon;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class ArrayListHUD extends Module implements TextRenderer {

    public static final NumberSetting animationSpeed = new NumberSetting("Animation", "The speed of the animation", 200, 0, 1000, 10);
    public static final ModeSetting<ArrayListColour> arrayListColour = new ModeSetting<>("Colour", "What colour to render the modules in", ArrayListColour.RAINBOW_WAVE);

    public ArrayListHUD() {
        super("ArrayList", ModuleCategory.HUD, "Renders the enabled modules on screen");
        this.addSettings(animationSpeed, arrayListColour);
    }

    @Override
    public void onRender2D() {
        ScaledResolution sr = new ScaledResolution(mc);
        float y = sr.getScaledHeight() - 11;

        int index = 0;

        // Creating a new list, so we can sort these, but not the module manager list
        List<Module> modules = new ArrayList<>();
        for (Module module : Paragon.INSTANCE.getModuleManager().getModules()) {
            if (module.animation.getAnimationFactor() > 0 && module.isVisible()) {
                modules.add(module);
            }
        }

        // Sort by module length
        modules.sort(Comparator.comparingDouble(module -> getStringWidth(module.getName() + module.getModuleInfo())));
        Collections.reverse(modules);

        for(Module module : modules) {
            renderText(module.getName() + formatCode(TextFormatting.GRAY) + module.getModuleInfo(), (float) (sr.getScaledWidth() - (((getStringWidth(module.getName() + module.getModuleInfo())) * module.animation.getAnimationFactor()) + 2)), y, arrayListColour.getCurrentMode().getColour(index * 150));
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
        SYNC((addition) -> Colours.mainColour.getColour().getRGB());

        private Function<Integer, Integer> colour;

        ArrayListColour(Function<Integer, Integer> colour) {
            this.colour = colour;
        }

        /**
         * Gets the colour
         * @param addition The addition to the colour
         * @return The colour
         */
        public int getColour(int addition) {
            return colour.apply(addition);
        }
    }
}
