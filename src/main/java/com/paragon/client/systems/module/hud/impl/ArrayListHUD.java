package com.paragon.client.systems.module.hud.impl;

import com.paragon.Paragon;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.ITextRenderer;
import com.paragon.api.module.Module;
import com.paragon.client.systems.module.hud.HUDEditorGUI;
import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.api.setting.Setting;
import com.paragon.client.ui.util.animation.Easing;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@SideOnly(Side.CLIENT)
public class ArrayListHUD extends HUDModule implements ITextRenderer {

    public static ArrayListHUD INSTANCE;

    public static Setting<Float> animationSpeed = new Setting<>("Animation", 200f, 0f, 1000f, 10f)
            .setDescription("The speed of the animation");

    public static Setting<ArrayListColour> arrayListColour = new Setting<>("Colour", ArrayListColour.RAINBOW_WAVE)
            .setDescription("What colour to render the modules in");

    public static Setting<Easing> easing = new Setting<>("Easing", Easing.EXPO_IN_OUT)
            .setDescription("The easing type of the animation");

    public static Setting<Boolean> background = new Setting<>("Background", false)
            .setDescription("Render a background behind the text");

    // Creating a new list, so we can sort these, but not the module manager list
    private final List<Module> enabledModules = new ArrayList<>();

    private Corner corner = Corner.TOP_LEFT;

    public ArrayListHUD() {
        super("ArrayList", "Renders the enabled modules on screen");

        INSTANCE = this;
    }

    @Override
    public void render() {
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        if (getX() + (getWidth() / 2) < scaledResolution.getScaledWidth() / 2f) {
            if (getY() + (getHeight() / 2) > scaledResolution.getScaledHeight() / 2f) {
                corner = Corner.BOTTOM_LEFT;
            } else {
                corner = Corner.TOP_LEFT;
            }
        }
        else if (getX() + (getWidth() / 2) > scaledResolution.getScaledWidth() / 2f) {
            if (getY() + (getHeight() / 2) < scaledResolution.getScaledHeight() / 2f) {
                corner = Corner.TOP_RIGHT;
            }

            else if (getY() + (getHeight() / 2) > scaledResolution.getScaledHeight() / 2f) {
                corner = Corner.BOTTOM_RIGHT;
            }
        }

        if (mc.currentScreen instanceof HUDEditorGUI) {
            RenderUtil.drawRect(getX(), getY(), getWidth() - 2, getHeight() - 2, 0x90000000);
            RenderUtil.drawBorder(getX(), getY(), getWidth() - 2, getHeight() - 2, 1, Colours.mainColour.getValue().getRGB());
        }

        enabledModules.clear();

        for (Module module : Paragon.INSTANCE.getModuleManager().getModules()) {
            if (module.getAnimation().getAnimationFactor() > 0 && module.isVisible()) {
                enabledModules.add(module);
            }
        }

        // Sort by module length
        enabledModules.sort(Comparator.comparingDouble(module -> getStringWidth(module.getName() + module.getData())));
        Collections.reverse(enabledModules);

        float x = getX();
        float topY = getY();
        float width = enabledModules.isEmpty() ? 0 : getStringWidth(enabledModules.get(0).getName() + enabledModules.get(0).getData()) + 4;

        if (corner.equals(Corner.BOTTOM_LEFT) || corner.equals(Corner.BOTTOM_RIGHT)) {
            topY = getY() - (enabledModules.size() * getFontHeight());
        }

        if (corner.equals(Corner.TOP_RIGHT) || corner.equals(Corner.BOTTOM_RIGHT)) {
            x = getX() - width;
        }

        RenderUtil.startGlScissor(x, topY, width * 1.5, enabledModules.size() * (getFontHeight() * 2));

        float yOffset = 0;

        switch (corner) {
            case TOP_LEFT: {
                int index = 0;

                for (Module module : enabledModules) {
                    float originX = getX() - (getStringWidth(module.getName() + module.getData()) + 4);
                    float textX = (float) (originX + ((getStringWidth(module.getName() + module.getData()) + 4) * module.getAnimation().getAnimationFactor()));

                    if (background.getValue()) {
                        RenderUtil.drawRect(textX, getY() + yOffset, getStringWidth(module.getName() + module.getData()) + 4, 11, 0x90000000);
                    }

                    renderText(module.getName() + formatCode(TextFormatting.GRAY) + module.getData(), textX + 2, getY() + yOffset + 1.5f, arrayListColour.getValue().getColour(index * 150));

                    yOffset += 11 * module.getAnimation().getAnimationFactor();
                    index++;
                }

                break;
            }

            case TOP_RIGHT: {
                int index = 0;

                for (Module module : enabledModules) {
                    float textX = (float) ((getX() + getWidth()) - ((getStringWidth(module.getName() + module.getData()) + 6) * module.getAnimation().getAnimationFactor()));

                    if (background.getValue()) {
                        RenderUtil.drawRect(textX, getY() + yOffset, getStringWidth(module.getName() + module.getData()) + 4, (float) (11 * module.getAnimation().getAnimationFactor()), 0x90000000);
                    }

                    renderText(module.getName() + formatCode(TextFormatting.GRAY) + module.getData(), textX + 2, getY() + yOffset + 1.5f, arrayListColour.getValue().getColour(index * 150));

                    yOffset += 11 * module.getAnimation().getAnimationFactor();
                    index++;
                }

                break;
            }

            case BOTTOM_RIGHT: {
                int index = 0;

                for (Module module : enabledModules) {
                    float textX = (float) ((getX() + getWidth()) - ((getStringWidth(module.getName() + module.getData()) + 6) * module.getAnimation().getAnimationFactor()));

                    if (background.getValue()) {
                        RenderUtil.drawRect(textX, getY() + yOffset, getStringWidth(module.getName() + module.getData()) + 4, 11, 0x90000000);
                    }

                    renderText(module.getName() + formatCode(TextFormatting.GRAY) + module.getData(), textX + 2, getY() + (getHeight() - getFontHeight()) + yOffset + 1.5f, arrayListColour.getValue().getColour(index * 150));

                    yOffset -= 11 * module.getAnimation().getAnimationFactor();
                    index++;
                }

                break;
            }

            case BOTTOM_LEFT: {
                int index = 0;

                for (Module module : enabledModules) {
                    float originX = getX() - (getStringWidth(module.getName() + module.getData()) + 4);
                    float textX = (float) (originX + ((getStringWidth(module.getName() + module.getData()) + 4) * module.getAnimation().getAnimationFactor()));

                    if (background.getValue()) {
                        RenderUtil.drawRect(textX, yOffset, getStringWidth(module.getName() + module.getData()) + 4, 11, 0x90000000);
                    }

                    renderText(module.getName() + formatCode(TextFormatting.GRAY) + module.getData(), textX + 2, getY() + (getHeight() - getFontHeight()) + yOffset + 1.5f, arrayListColour.getValue().getColour(index * 150));

                    yOffset -= 11 * module.getAnimation().getAnimationFactor();
                    index++;
                }

                break;
            }
        }

        this.setHeight(Math.abs(yOffset));

        if (this.getHeight() > scaledResolution.getScaledHeight()) {
            this.setHeight(scaledResolution.getScaledHeight());
        }

        RenderUtil.endGlScissor();
    }

    @Override
    public float getWidth() {
        return 56;
    }

    @Override
    public float getHeight() {
        return 56;
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

    public enum Corner {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }
}
