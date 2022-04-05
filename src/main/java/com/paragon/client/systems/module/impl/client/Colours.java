package com.paragon.client.systems.module.impl.client;

import com.paragon.client.systems.module.Constant;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.ColourSetting;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;

import java.awt.*;

@Constant
public class Colours extends Module {

    public static ModeSetting<ColourMode> mode = new ModeSetting<>("Colour Mode", "What colour to use", ColourMode.STATIC);

    // Normal colour
    public static ColourSetting mainColour = (ColourSetting) new ColourSetting("Main Colour", "The main colour of the client", new Color(185, 19, 211)).setVisiblity(() -> mode.getCurrentMode().equals(ColourMode.STATIC));

    // Rainbow
    private final NumberSetting rainbowSpeed = (NumberSetting) new NumberSetting("Speed", "The speed of the rainbow", 4, 1, 10, 0.1f).setVisiblity(() -> mode.getCurrentMode().equals(ColourMode.RAINBOW));
    private final NumberSetting rainbowSaturation = (NumberSetting) new NumberSetting("Saturation", "The saturation of the rainbow", 100, 1, 100, 1).setVisiblity(() -> mode.getCurrentMode().equals(ColourMode.RAINBOW));

    public Colours() {
        super("Colours", ModuleCategory.CLIENT, "Customise the client's main colour");
        this.addSettings(mode, mainColour, rainbowSpeed, rainbowSaturation);
    }

    @Override
    public void onTick() {
        mainColour.setRainbowSpeed(rainbowSpeed.getValue());
        mainColour.setRainbowSaturation(rainbowSaturation.getValue() / 100f);
        mainColour.setRainbow(mode.getCurrentMode().equals(ColourMode.RAINBOW));
    }

    public enum ColourMode {
        /**
         * A static (non-changing) colour
         */
        STATIC,

        /**
         * A rainbow colour
         */
        RAINBOW
    }
}
