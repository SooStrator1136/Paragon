package com.paragon.client.systems.module.impl.render;

import com.paragon.Paragon;
import com.paragon.api.event.render.entity.RenderNametagEvent;
import com.paragon.api.util.entity.EntityUtil;
import com.paragon.api.util.player.EntityFakePlayer;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.asm.mixins.accessor.IRenderManager;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.impl.client.ClientFont;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Wolfsurge
 */
public class Nametags extends Module implements TextRenderer {

    // Render settings
    private final BooleanSetting health = new BooleanSetting("Health", "Render the player's health", true);
    private final BooleanSetting ping = new BooleanSetting("Ping", "Render the player's ping", true);
    private final BooleanSetting pops = new BooleanSetting("Pops", "Render the player's totem pop count", true);

    private final BooleanSetting armour = new BooleanSetting("Armour", "Render the player's armour", true);
    private final BooleanSetting armourDurability = (BooleanSetting) new BooleanSetting("Durability", "Render the player's armour durability", true).setParentSetting(armour);

    // Scaling
    private final NumberSetting scaleFactor = new NumberSetting("Scale", "The scale of the nametag", 0.2f, 0.1f, 1f, 0.1f);
    private final BooleanSetting distanceScale = new BooleanSetting("Distance Scale", "Scale the nametag based on your distance from the player", true);

    public Nametags() {
        super("Nametags", ModuleCategory.RENDER, "Draws nametags above players");
        this.addSettings(health, ping, pops, armour, scaleFactor, distanceScale);
    }

    @Override
    public void onRender3D() {
        // Prevent null pointer exceptions
        if (nullCheck() || mc.player.connection == null || mc.player.ticksExisted < 20) {
            return;
        }

        // Iterate through loaded players
        for (EntityPlayer player : mc.world.playerEntities) {
            // Check the player isn't us
            if (player == mc.player) {
                // continue;
            }

            // Get render x, y, and z
            double[] renderValues = {((IRenderManager) mc.getRenderManager()).getRenderX(), ((IRenderManager) mc.getRenderManager()).getRenderY(), ((IRenderManager) mc.getRenderManager()).getRenderZ()};
            // Get player interpolated position
            Vec3d renderVec = EntityUtil.getInterpolatedPosition(player);

            // Get scale
            double distance = mc.player.getDistance(renderVec.x, renderVec.y, renderVec.z);
            float scale = (scaleFactor.getValue() * 5) / 50f;

            if (distanceScale.isEnabled()) {
                scale = (float) (Math.max(scaleFactor.getValue() * 5, scaleFactor.getValue() * distance) / 50);
            }

            // Translate, rotate, and scale
            glPushMatrix();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.disableLighting();
            glTranslated(renderVec.x - renderValues[0], renderVec.y + player.height + (player.isSneaking() ? 0.05 : 0.08) - renderValues[1], renderVec.z - renderValues[2]);
            glRotated(-mc.getRenderManager().playerViewY, 0, 1, 0);
            glRotated(mc.getRenderManager().playerViewX, (mc.gameSettings.thirdPersonView == 2) ? -1 : 1, 0, 0);
            glScaled(-scale, -scale, scale);

            // Disable depth so we can see the nametag through walls
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();

            // Get ping and pop count
            int playerPing = mc.player.connection.getPlayerInfo(player.getUniqueID()).getResponseTime();
            int popCount = (player instanceof EntityFakePlayer ? 0 : Paragon.INSTANCE.getPopManager().getPops(player));

            // Build string
            String renderString = player.getName() +
                    (health.isEnabled() ? " " + EntityUtil.getTextColourFromEntityHealth(player) + Math.round(EntityUtil.getEntityHealth(player)) : "")
                    + (ping.isEnabled() ? " " + getPingColour(playerPing) + playerPing : "")
                    + (pops.isEnabled() ? " " + TextFormatting.GOLD + popCount : "");

            // Get nametag width
            float width = getStringWidth(renderString) + 4;

            // Center nametag
            glTranslated(-width / 2, -20, 0);

            // Draw background
            RenderUtil.drawRect(0, 0, width, getFontHeight() + (ClientFont.INSTANCE.isEnabled() ? 0 : 2), 0x90000000);
            RenderUtil.drawBorder(0, 0, width, getFontHeight() + (ClientFont.INSTANCE.isEnabled() ? 0 : 2), 0.5f, -1);

            // Render string
            renderText(renderString, 2, 2, -1);

            // Render armour
            if (armour.isEnabled()) {
                // Get the items we want to render
                ArrayList<ItemStack> stacks = new ArrayList<>();
                stacks.add(mc.player.getHeldItemMainhand());
                Collections.reverse(mc.player.inventory.armorInventory);
                stacks.addAll(mc.player.inventory.armorInventory);
                Collections.reverse(mc.player.inventory.armorInventory);
                stacks.add(mc.player.getHeldItemOffhand());

                // Get armour count
                int count = 0;
                for (ItemStack stack : stacks) {
                    if (stack.isEmpty()) {
                        continue;
                    }

                    count++;
                }

                // Center armour
                int armourX = (int) ((width / 2) - ((count * 18) / 2));

                // Render armour
                for (ItemStack stack : stacks) {
                    // Check the stack isn't empty
                    if(stack.getItem() != Items.AIR) {
                        // Y value
                        int y = -20;

                        glPushMatrix();
                        glDepthMask(true);
                        GlStateManager.clear(256);
                        GlStateManager.disableDepth();
                        GlStateManager.enableDepth();
                        RenderHelper.enableStandardItemLighting();
                        mc.getRenderItem().zLevel = -100.0F;
                        GlStateManager.scale(1, 1, 0.01f);

                        // Render the armour
                        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, armourX, y);
                        mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, armourX, y);

                        mc.getRenderItem().zLevel = 0.0F;
                        GlStateManager.scale(1, 1, 1);
                        RenderHelper.disableStandardItemLighting();
                        GlStateManager.enableAlpha();
                        GlStateManager.disableBlend();
                        GlStateManager.disableLighting();
                        GlStateManager.scale(0.5D, 0.5D, 0.5D);
                        GlStateManager.disableDepth();

                        float yOffset = 25;

                        // Render the armour's durability
                        if (armourDurability.isEnabled() && stack.getItem() instanceof ItemArmor || stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemTool) {
                            float green = ((float) stack.getMaxDamage() - (float) stack.getItemDamage()) / (float) stack.getMaxDamage();
                            float red = 1 - green;
                            int damage = 100 - (int) (red * 100);
                            renderText(damage + "%", armourX * 2 + 4, y - yOffset, (new Color(red, green, 0)).getRGB());
                            yOffset -= getFontHeight();
                        }

                        // Render enchants
                        NBTTagList enchants = stack.getEnchantmentTagList();
                        for (int i = 0; i < enchants.tagCount(); i++) {
                            // Get enchant ID and level
                            int id = enchants.getCompoundTagAt(i).getInteger("id");
                            int level = enchants.getCompoundTagAt(i).getInteger("lvl");

                            // Get the enchantment
                            Enchantment enchantment = Enchantment.getEnchantmentByID(id);

                            // Make sure the enchantment is valid
                            if (enchantment != null) {
                                // Don't render if it's a curse
                                if (enchantment.isCurse()) {
                                    continue;
                                }

                                // Get enchantment name
                                String enchantmentName = enchantment.getTranslatedName(level).substring(0, 4) + (level == 1 ? "" : level);

                                // Render the enchantment's name and level
                                renderText(enchantmentName, armourX * 2 + 4, y - yOffset, 0xFFFFFF);
                                yOffset -= getFontHeight();
                            }
                        }

                        // Re enable depth
                        GlStateManager.enableDepth();
                        GlStateManager.scale(2.0F, 2.0F, 2.0F);
                        glPopMatrix();

                        // Increase X value
                        armourX += 18;
                    }
                }
            }

            // End render
            GlStateManager.enableDepth();
            GlStateManager.disableBlend();
            glPopMatrix();
        }
    }

    public TextFormatting getPingColour(int ping) {
        if (ping < 0) {
            return TextFormatting.RED;
        } else if (ping < 50) {
            return TextFormatting.GREEN;
        } else if (ping < 150) {
            return TextFormatting.YELLOW;
        } else {
            return TextFormatting.RED;
        }
    }

    @Listener
    public void onRenderNametag(RenderNametagEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            event.cancel();
        }
    }
}
