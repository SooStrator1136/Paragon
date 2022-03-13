package com.paragon.client.systems.module.impl.client;

import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.ColourSetting;

import java.awt.*;

public class Colours extends Module {

    public static ColourSetting mainColour = new ColourSetting("Main Colour", "The main colour of the client", new Color(185, 19, 211));

    public Colours() {
        super("Colours", ModuleCategory.CLIENT, "Customise the client's main colour");
        this.addSettings(mainColour);
    }
}
