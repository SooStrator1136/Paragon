package com.paragon.client.systems.module.hud.impl;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.impl.client.Colours;
import net.minecraft.item.ItemStack;

/**
 * @author Wolfsurge
 */
public class Inventory extends HUDModule {

    public static Inventory INSTANCE;

    public Inventory() {
        super("Inventory", "Displays the contents of your inventory");

        INSTANCE = this;
    }

    @Override
    public void render() {
        RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight() - 4, 0x90000000);
        RenderUtil.drawBorder(getX(), getY(), getWidth(), getHeight() - 4, 1, Colours.mainColour.getValue().getRGB());

        float x = 0;
        float y = 0;
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            RenderUtil.renderItemStack(stack, getX() + x, getY() + y, true);

            x += 18;

            // cba for calcs
            if (i == 17 || i == 26 || i == 35) {
                x = 0;
                y += 18;
            }
        }
    }

    @Override
    public float getWidth() {
        return 18 * 9;
    }

    @Override
    public float getHeight() {
        return 18 * 3 + 4;
    }
}
