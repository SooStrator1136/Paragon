package com.paragon.client.systems.module.impl.client;

import com.paragon.Paragon;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unchecked")
public class HUD extends Module implements TextRenderer {

    // Watermark
    private final BooleanSetting watermark = new BooleanSetting("Watermark", "Draws the client's name in the top left", true);

    // Array list
    private static final BooleanSetting arrayList = new BooleanSetting("Array List", "Render the enabled modules on screen", true);
    public static final NumberSetting animationSpeed = (NumberSetting) new NumberSetting("Animation", "The speed of the animation", 200, 0, 1000, 10).setParentSetting(arrayList);
    private static final ModeSetting<ArrayListColour> arrayListColour = (ModeSetting<ArrayListColour>) new ModeSetting<>("Colour", "What colour to render the modules in", ArrayListColour.RAINBOW_WAVE).setParentSetting(arrayList);

    // Info
    private final BooleanSetting info = new BooleanSetting("Info", "Render useful information in the bottom left", true);
    private final BooleanSetting fps = (BooleanSetting) new BooleanSetting("FPS", "Render your FPS", true).setParentSetting(info);

    public HUD() {
        super("HUD", ModuleCategory.CLIENT, "Render the client's HUD on screen");
        this.addSettings(watermark, arrayList, info);
    }

    @Override
    public void onRender2D() {
        drawWatermark();
        drawInfo();
        drawArrayList();
    }

    public void drawWatermark() {
        if (watermark.isEnabled()) {
            renderText("Paragon " + formatCode(TextFormatting.GRAY) + Paragon.modVersion, 3, 3, Colours.mainColour.getColour().getRGB());
        }
    }

    public void drawInfo() {
        if (info.isEnabled()) {
            ScaledResolution scaledResolution = new ScaledResolution(mc);

            float y = scaledResolution.getScaledHeight() - 11;

            if (fps.isEnabled()) {
                renderText("FPS " + formatCode(TextFormatting.GRAY) + Minecraft.getDebugFPS(), 2, y, Colours.mainColour.getColour().getRGB());
                y -= 11;
            }
        }
    }

    public void drawArrayList() {
        if(arrayList.isEnabled()) {
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
                renderText(module.getName() + formatCode(TextFormatting.GRAY) + module.getModuleInfo(), sr.getScaledWidth() - (((getStringWidth(module.getName() + module.getModuleInfo())) * module.animation.getAnimationFactor()) + 2), y, arrayListColour.getCurrentMode().getColour(index * 150));
                y -= 11 * module.animation.getAnimationFactor();
                index++;
            }
        }
    }

    public enum ArrayListColour {
        /**
         * The colour is slightly different for each module in the array list
         */
        RAINBOW_WAVE,

        /**
         * All modules are rainbow
         */
        RAINBOW,

        /**
         * Permanent static colour
         */
        STATIC;

        /**
         * Get the module colour
         * @param addition The added colour addition
         * @return The module colour
         */
        public int getColour(int addition) {
            switch (HUD.arrayListColour.getCurrentMode()) {
                case RAINBOW_WAVE:
                    return ColourUtil.getRainbow(4, 1, addition);
                case RAINBOW:
                    return ColourUtil.getRainbow(4, 1, 0);
                case STATIC:
                    return Colours.mainColour.getColour().getRGB();
            }

            return -1;
        }
    }
}
