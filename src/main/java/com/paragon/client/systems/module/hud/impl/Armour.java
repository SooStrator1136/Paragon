package com.paragon.client.systems.module.hud.impl;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.world.BlockUtil;
import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Armour extends HUDModule {

    public static Armour INSTANCE;

    public static Setting<Boolean> waterOffset = new Setting<>("Water Offset", true)
            .setDescription("Position higher when you are underwater");

    public Armour() {
        super("Armour", "Displays your armour on screen");

        INSTANCE = this;
    }


    @Override
    public void render() {
        List<ItemStack> armourList = new ArrayList<>(mc.player.inventory.armorInventory);
        Collections.reverse(armourList);

        GL11.glPushMatrix();
        GL11.glTranslatef(0, waterOffset.getValue() &&
                // We do this rather than mc.player.isInWater() because we only want to offset if we can see the bubbles (top and bottom half of player is in water)
                // This checks that the top half of the player is in water
                BlockUtil.getBlockAtPos(mc.player.getPosition().up()).equals(Blocks.WATER) ? -10 : 0, 0);

        float xSpacing = 0;
        for (ItemStack itemStack : armourList) {
            // We don't want to render stack
            if (itemStack.isEmpty()) {
                xSpacing += 18;
                continue;
            }

            // Render stack
            RenderUtil.renderItemStack(itemStack, (getX() + xSpacing), getY() + 4, true);

            // Get the item's damage percentage
            int itemDamage = (int) (100 - ((1 - (((float) itemStack.getMaxDamage() - (float) itemStack.getItemDamage()) / (float) itemStack.getMaxDamage())) * 100));

            // Scale
            GL11.glScalef(0.75f, 0.75f, 0.75f);
            float scaleFactor = 1 / 0.75f;

            // Render the damage percentage
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(String.valueOf(itemDamage), (getX() + xSpacing + (9 - (getStringWidth(String.valueOf(itemDamage)) / 2f))) * scaleFactor, getY() * scaleFactor, -1);

            GL11.glScalef(scaleFactor, scaleFactor, scaleFactor);

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
