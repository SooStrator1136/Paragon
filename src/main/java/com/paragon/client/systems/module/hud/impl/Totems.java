package com.paragon.client.systems.module.hud.impl;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.impl.client.Colours;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class Totems extends HUDModule {

    public static Totems INSTANCE;

    public Totems() {
        super("Totems", "Displays the amount of totems in your inventory");

        INSTANCE = this;
    }

    @Override
    public void render() {
        RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), 0x70000000);
        RenderUtil.drawBorder(getX(), getY(), getWidth(), getHeight(), 1, Colours.mainColour.getValue().getRGB());

        ItemStack itemStack = new ItemStack(Items.TOTEM_OF_UNDYING, getTotems());

        RenderUtil.renderItemStack(itemStack, getX() + 1, getY() + 2, true);
    }

    @Override
    public float getWidth() {
        return 19;
    }

    @Override
    public float getHeight() {
        return 19;
    }

    public int getTotems() {
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

        return count;
    }
}
