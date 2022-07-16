package com.paragon.client.systems.module.hud.impl;

import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.systems.module.impl.combat.Aura;
import com.paragon.client.systems.module.impl.combat.AutoCrystal;
import com.paragon.client.systems.module.impl.combat.AutoCrystalRewrite;
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
        renderText("CA " + (AutoCrystal.INSTANCE.isEnabled() ? TextFormatting.GREEN + "Enabled" : TextFormatting.RED + "Disabled"), getX(), getY() + getFontHeight(), Colours.mainColour.getValue().getRGB());
        renderText("ACR " + (AutoCrystalRewrite.INSTANCE.isEnabled() ? TextFormatting.GREEN + "Enabled" : TextFormatting.RED + "Disabled"), getX(), getY() + getFontHeight() * 2, Colours.mainColour.getValue().getRGB());
    }

    @Override
    public float getWidth() {
        // Longest
        return getStringWidth("ACR Disabled");
    }

    @Override
    public float getHeight() {
        return getFontHeight() * 3;
    }
}
