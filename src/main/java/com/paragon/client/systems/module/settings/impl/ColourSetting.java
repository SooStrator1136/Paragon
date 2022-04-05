package com.paragon.client.systems.module.settings.impl;

import com.paragon.api.util.render.ColourUtil;
import com.paragon.client.systems.module.settings.Setting;

import java.awt.*;

public class ColourSetting extends Setting {

    private Color colour;
    private boolean rainbow;
    private float rainbowSpeed = 4;
    private float rainbowSaturation = 1;

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
            return new Color(ColourUtil.getRainbow(rainbowSpeed, rainbowSaturation, 0));
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
     * Sets the current colour to rainbow
     * @param rainbow Whether or not the colour is rainbow
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
