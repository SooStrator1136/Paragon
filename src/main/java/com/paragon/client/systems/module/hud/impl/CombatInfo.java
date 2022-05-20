package com.paragon.client.systems.module.hud.impl;

import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.systems.module.impl.combat.Aura;
import com.paragon.client.systems.module.impl.combat.AutoCrystal;
import net.minecraft.util.text.TextFormatting;

public class CombatInfo extends HUDModule {

    public static CombatInfo INSTANCE;

    public CombatInfo() {
        super("CombatInfo", "Shows what combat modules are enabled");

        INSTANCE = this;
    }

    @Override
    public void render() {
        renderText("KA " + (Aura.INSTANCE.isEnabled() ? TextFormatting.GREEN + "Enabled" : TextFormatting.RED + "Disabled"), getX(), getY(), Colours.mainColour.getValue().getRGB());
        renderText("CA " + (AutoCrystal.INSTANCE.isEnabled() ? TextFormatting.GREEN + "Enabled" : TextFormatting.RED + "Disabled"), getX(), getY() + 10, Colours.mainColour.getValue().getRGB());
    }

    @Override
    public float getWidth() {
        // Basically the same for both
        return getStringWidth("KA Disabled");
    }

    @Override
    public float getHeight() {
        return 20;
    }
}
