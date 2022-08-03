package com.paragon.client.systems.module.hud.impl;

import com.paragon.api.util.render.font.FontUtil;
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
        FontUtil.drawStringWithShadow("KA " + (Aura.INSTANCE.isEnabled() ? TextFormatting.GREEN + "Enabled" : TextFormatting.RED + "Disabled"), getX(), getY(), Colours.mainColour.getValue().getRGB());
        FontUtil.drawStringWithShadow("CA " + (AutoCrystal.INSTANCE.isEnabled() ? TextFormatting.GREEN + "Enabled" : TextFormatting.RED + "Disabled"), getX(), getY() + FontUtil.getHeight(), Colours.mainColour.getValue().getRGB());
        FontUtil.drawStringWithShadow("ACR " + (AutoCrystalRewrite.INSTANCE.isEnabled() ? TextFormatting.GREEN + "Enabled" : TextFormatting.RED + "Disabled"), getX(), getY() + FontUtil.getHeight() * 2, Colours.mainColour.getValue().getRGB());
    }

    @Override
    public float getWidth() {
        // Longest
        return FontUtil.getStringWidth("ACR Disabled");
    }

    @Override
    public float getHeight() {
        return FontUtil.getHeight() * 3;
    }

}
