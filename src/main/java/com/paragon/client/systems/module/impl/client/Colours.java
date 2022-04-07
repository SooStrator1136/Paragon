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

    public static ColourSetting mainColour = (ColourSetting) new ColourSetting("Main Colour", "The main colour of the client", new Color(185, 19, 211));

    public Colours() {
        super("Colours", ModuleCategory.CLIENT, "Customise the client's main colour");
        this.addSettings(mainColour);
    }

}
