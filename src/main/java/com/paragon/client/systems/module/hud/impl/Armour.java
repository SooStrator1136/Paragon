package com.paragon.client.systems.module.hud.impl;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.module.hud.HUDModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Armour extends HUDModule {

    public Armour() {
        super("Armour", "Displays your armour on screen");
    }


    @Override
    public void render() {
        List<ItemStack> armourList = new ArrayList<>(mc.player.inventory.armorInventory);
        Collections.reverse(armourList);

        GL11.glPushMatrix();

        float xSpacing = 0;
        for (ItemStack itemStack : armourList) {
            if (itemStack.isEmpty()) {
                continue;
            }

            RenderUtil.renderItemStack(itemStack, (getX() + xSpacing), getY() + 4, true);

            int itemDamage = (int) (100 - ((1 - (((float) itemStack.getMaxDamage() - (float) itemStack.getItemDamage()) / (float) itemStack.getMaxDamage())) * 100));

            GL11.glScalef(0.75f, 0.75f, 0.75f);
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(String.valueOf(itemDamage), (getX() + xSpacing + (9 - (getStringWidth(String.valueOf(itemDamage)) / 2f))) * 1.333333333333333f, getY() * 1.333333333333333f, -1);
            GL11.glScalef(1.333333333333333f, 1.333333333333333f, 1.333333333333333f);

            xSpacing += 18;
        }

        GL11.glPopMatrix();
    }

    @Override
    public float getWidth() {
        return 18 * 4;
    }

    @Override
    public float getHeight() {
        return 22;
    }
}
