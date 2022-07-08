package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.module.Category;
import com.paragon.api.module.Module;
import com.paragon.api.setting.Setting;

public class ExtraTab extends Module {

    public static Setting<Float> limit = new Setting<>("Limit", 500f, 1f, 500f, 1f)
            .setDescription("The limit of players");

    public ExtraTab() {
        super("ExtraTab", Category.MISC, "Extends the limit of players on the tab list");
    }

}
