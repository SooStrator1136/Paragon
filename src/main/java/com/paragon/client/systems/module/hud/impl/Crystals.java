package com.paragon.client.systems.module.hud.impl;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.impl.client.Colours;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class Crystals extends HUDModule {

    public static Crystals INSTANCE;

    public Crystals() {
        super("Crystals", "Displays the amount of crystals in your inventory");

        INSTANCE = this;
    }

    @Override
    public void render() {
        RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), 0x70000000);
        RenderUtil.drawBorder(getX(), getY(), getWidth(), getHeight(), 1, Colours.mainColour.getValue().getRGB());

        ItemStack itemStack = new ItemStack(Items.END_CRYSTAL, getCrystals());

        RenderUtil.renderItemStack(itemStack, getX() + getWidth() - 18, getY() + 2, true);
    }

    @Override
    public float getWidth() {
        return MathHelper.clamp(mc.fontRenderer.getStringWidth(String.valueOf(getCrystals())), 18, mc.fontRenderer.getStringWidth(String.valueOf(getCrystals()))) + 2;
    }

    @Override
    public float getHeight() {
        return 19;
    }

    public int getCrystals() {
        int count = 0;

        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);

            if (itemStack.getItem() == Items.END_CRYSTAL) {
                count += itemStack.getCount();
            }
        }

        if (mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) {
            count += mc.player.getHeldItemMainhand().getCount();
        }

        if (mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
            count += mc.player.getHeldItemOffhand().getCount();
        }

        return count;
    }
}
