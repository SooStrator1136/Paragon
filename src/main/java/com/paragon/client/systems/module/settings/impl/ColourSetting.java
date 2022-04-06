package com.paragon.client.systems.module.settings.impl;

import com.paragon.api.util.render.ColourUtil;
import com.paragon.client.systems.module.settings.Setting;

import java.awt.*;

public class ColourSetting extends Setting {

    private Color colour;
    private boolean rainbow;
    private float rainbowSpeed = 4;
    private float rainbowSaturation = 100;

    /**
     * Creates a new colour setting
     * @param name The name of the setting
     * @param description The description of the setting
     * @param defaultColour The default colour
     */
    public ColourSetting(String name, String description, Color defaultColour) {
        setName(name);
        setDescription(description);
        setColour(defaultColour);
    }

    /**
     * Gets the current colour
     */
    public Color getColour() {
        if (rainbow) {
            return ColourUtil.integrateAlpha(new Color(ColourUtil.getRainbow(rainbowSpeed, rainbowSaturation / 100, 0)), colour.getAlpha());
        }

        return colour;
    }

    /**
     * Sets the current colour
     * @param newColour The new colour
     */
    public void setColour(Color newColour) {
        this.colour = newColour;
    }

    /**
     * Gets whether the colour is rainbow
     * @return Whether the colour is rainbow
     */
    public boolean isRainbow() {
        return rainbow;
    }

    /**
     * Gets the rainbow's saturation
     * @return The rainbow's saturation
     */
    public float getRainbowSaturation() {
        return rainbowSaturation;
    }

    /**
     * Gets the rainbow's speed
     * @return Gets the rainbow's speed
     */
    public float getRainbowSpeed() {
        return rainbowSpeed;
    }

    /**
     * Sets the current colour to rainbow
     * @param rainbow Whether the colour is a rainbow
     */
    public void setRainbow(boolean rainbow) {
        this.rainbow = rainbow;
    }

    /**
     * Sets the speed of the rainbow
     * @param speed The speed
     */
    public void setRainbowSpeed(float speed) {
        this.rainbowSpeed = speed;
    }

    /**
     * Sets the saturation of the rainbow
     * @param saturation The saturation
     */
    public void setRainbowSaturation(float saturation) {
        this.rainbowSaturation = saturation;
    }

}
