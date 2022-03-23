package com.paragon.client.systems.module.hud.impl;

import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.impl.client.Colours;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class Totems extends HUDModule {

    public Totems() {
        super("Totems", "Displays the amount of totems in your inventory");
    }

    @Override
    public void render() {
        renderText(getText(), getX(), getY(), Colours.mainColour.getColour().getRGB());
    }

    @Override
    public float getWidth() {
        return getStringWidth(getText());
    }

    @Override
    public float getHeight() {
        return getFontHeight();
    }

    public String getText() {
        int count = 0;

        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);

            if (itemStack.getItem() == Items.TOTEM_OF_UNDYING) {
                count++;
            }
        }

        if (mc.player.getHeldItemMainhand().getItem() == Items.TOTEM_OF_UNDYING) {
            count++;
        }

        if (mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING) {
            count++;
        }

        return "Totems " + TextFormatting.WHITE + count;
    }
}
