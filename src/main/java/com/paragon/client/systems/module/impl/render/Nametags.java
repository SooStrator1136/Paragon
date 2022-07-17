package com.paragon.client.systems.module.impl.render;

import com.paragon.Paragon;
import com.paragon.api.event.render.entity.RenderNametagEvent;
import com.paragon.api.util.entity.EntityUtil;
import com.paragon.api.util.player.EntityFakePlayer;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.ITextRenderer;
import com.paragon.asm.mixins.accessor.IRenderManager;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import com.paragon.client.systems.module.impl.client.ClientFont;
import com.paragon.api.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Wolfsurge
 */
public class Nametags extends Module implements ITextRenderer {

    public static Nametags INSTANCE;

    // Render settings
    public static Setting<Boolean> health = new Setting<>("Health", true)
            .setDescription("Render the player's health");

    public static Setting<Boolean> ping = new Setting<>("Ping", true)
            .setDescription("Render the player's ping");

    public static Setting<Boolean> pops = new Setting<>("Pops", true)
            .setDescription("Render the player's totem pop count");

    public static Setting<Boolean> armour = new Setting<>("Armour", true)
            .setDescription("Render the player's armour");

    public static Setting<Boolean> armourDurability = new Setting<>("Durability", true)
            .setDescription("Render the player's armour durability")
            .setParentSetting(armour);

    public static Setting<Boolean> potions = new Setting<>("Potions", true)
            .setDescription("Shows the player's potion effects");

    // Scaling
    public static Setting<Float> scaleFactor = new Setting<>("Scale", 0.2f, 0.1f, 1f, 0.1f)
            .setDescription("The scale of the nametag");

    public static Setting<Boolean> distanceScale = new Setting<>("DistanceScale", true)
            .setDescription("Scale the nametag based on your distance from the player");

    public static Setting<Boolean> outline = new Setting<>("Outline", true)
            .setDescription("Render the nametag outline");

    public static Setting<Float> outlineWidth = new Setting<>("Width", 0.5f, 0.1f, 2f, 0.01f)
            .setDescription("The width of the outline")
            .setParentSetting(outline);

    public static Setting<Color> outlineColour = new Setting<>("Colour", new Color(185, 17, 255))
            .setDescription("The colour of the outline")
            .setParentSetting(outline);

    public Nametags() {
        super("Nametags", Category.RENDER, "Draws nametags above players");

        INSTANCE = this;
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
                continue;
            }

            // Get render x, y, and z
            double[] renderValues = {((IRenderManager) mc.getRenderManager()).getRenderX(), ((IRenderManager) mc.getRenderManager()).getRenderY(), ((IRenderManager) mc.getRenderManager()).getRenderZ()};
            // Get player interpolated position
            Vec3d renderVec = EntityUtil.getInterpolatedPosition(player);

            // Get scale
            double distance = mc.player.getDistance(renderVec.x, renderVec.y, renderVec.z);
            float scale = (scaleFactor.getValue() * 5) / 50f;

            if (distanceScale.getValue()) {
                scale = (float) (Math.max(scaleFactor.getValue() * 5, scaleFactor.getValue() * distance) / 50);
            }

            // Translate, rotate, and scale
            glPushMatrix();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.disableLighting();
            glTranslated(renderVec.x - renderValues[0], renderVec.y + player.height + 0.1 + (player.isSneaking() ? 0.05 : 0.08) - renderValues[1], renderVec.z - renderValues[2]);
            glRotated(-mc.getRenderManager().playerViewY, 0, 1, 0);
            glRotated(mc.getRenderManager().playerViewX, (mc.gameSettings.thirdPersonView == 2) ? -1 : 1, 0, 0);
            glScaled(-scale, -scale, scale);

            // Disable depth so we can see the nametag through walls
            GlStateManager.disableDepth();

            StringBuilder stringBuilder = new StringBuilder(player.getName());

            if (health.getValue()) {
                stringBuilder.append(" ").append(EntityUtil.getTextColourFromEntityHealth(player)).append(Math.round(EntityUtil.getEntityHealth(player)));
            }

            if (ping.getValue() && mc.getConnection() != null) {
                mc.getConnection().getPlayerInfo(player.getUniqueID());
                stringBuilder.append(" ").append(getPingColour(mc.getConnection().getPlayerInfo(player.getUniqueID()).getResponseTime())).append(mc.getConnection().getPlayerInfo(player.getUniqueID()).getResponseTime());
            }

            if (pops.getValue()) {
                stringBuilder.append(" ").append(TextFormatting.GOLD).append("-").append((player instanceof EntityFakePlayer) ? 0 : Paragon.INSTANCE.getPopManager().getPops(player));
            }

            float potionWidth = 0;
            List<String> potionStrings = new ArrayList<>();

            player.getActivePotionEffects().stream().sorted(Comparator.comparing(PotionEffect::getDuration)).forEach(potion -> {
                        if (potions.getValue()) {
                            Potion potionType = Potion.getPotionFromResourceLocation(potion.getPotion().getRegistryName().toString());
                            if (potionType != null) {
                                potionStrings.add(potionType.getName().replace("potion.", "") + " " + potion.getDuration() + "s");
                            }
                        }
                    });

            potionStrings.sort(Comparator.comparingDouble(this::getStringWidth));

            // Get nametag width
            float width = getStringWidth(stringBuilder.toString()) + potionWidth + 4;

            // Center nametag
            glTranslated(-width / 2, -20, 0);

            // Draw background
            RenderUtil.drawRect(0, 0, width, getFontHeight() + (ClientFont.INSTANCE.isEnabled() ? 0 : 2), 0x90000000);

            // Draw border
            if (outline.getValue()) {
                RenderUtil.drawBorder(0, 0, width, getFontHeight() + (ClientFont.INSTANCE.isEnabled() ? 0 : 2), outlineWidth.getValue(), outlineColour.getValue().getRGB());
            }

            // Render string
            renderText(stringBuilder.toString(), 2, 2, -1);

            // Render armour
            if (armour.getValue()) {
                // Get the items we want to render
                ArrayList<ItemStack> stacks = new ArrayList<>();
                stacks.add(player.getHeldItemMainhand());
                Collections.reverse(player.inventory.armorInventory);
                stacks.addAll(player.inventory.armorInventory);
                Collections.reverse(player.inventory.armorInventory);
                stacks.add(player.getHeldItemOffhand());

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
                    if (stack.getItem() != Items.AIR) {
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

                        List<String> itemInfo = new ArrayList<>();

                        // Render the armour's durability
                        if (armourDurability.getValue() && stack.getItem() instanceof ItemArmor || stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemTool) {
                            float damage = 1 - (float) stack.getItemDamage() / (float) stack.getMaxDamage();
                            itemInfo.add((int) (damage * 100) + "%");
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
                                itemInfo.add(enchantmentName);
                            }
                        }

                        float yOffset = -(itemInfo.size() * getFontHeight()) + 15;
                        for (String info : itemInfo) {
                            int colour = -1;

                            if (info.equals(itemInfo.get(0))) {
                                float green = ((float) stack.getMaxDamage() - (float) stack.getItemDamage()) / (float) stack.getMaxDamage();
                                float red = 1 - green;

                                colour = new Color(red, green, 0, 1).getRGB();
                            }

                            renderText(info, armourX * 2 + 4, y + yOffset, colour);
                            yOffset += getFontHeight();
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

            float y = 0;

            // GL11.glScalef(0.75f, 0.75f, 0.75f);
            // float scaleFactor = 1 / 0.75f;

            for (String effectString : potionStrings) {
                renderText(effectString, (getStringWidth(stringBuilder.toString()) + 6), y, -1);

                y += getFontHeight();
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
