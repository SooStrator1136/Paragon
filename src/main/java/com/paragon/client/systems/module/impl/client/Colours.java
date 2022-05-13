package com.paragon.client.systems.module.impl.client;

import com.paragon.client.systems.module.Constant;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;

import java.awt.*;

@Constant
public class Colours extends Module {

    public static Setting<Color> mainColour = new Setting<>("Main Colour", new Color(185, 19, 211))
            .setDescription("The main colour of the client");

    public Colours() {
        super("Colours", Category.CLIENT, "Customise the client's main colour");
        this.addSettings(mainColour);
    }

}
