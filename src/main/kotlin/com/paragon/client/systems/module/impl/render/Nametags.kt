@file:Suppress("ReplaceNotNullAssertionWithElvisReturn")

package com.paragon.client.systems.module.impl.render

import com.paragon.Paragon
import com.paragon.api.event.render.entity.RenderNametagEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.entity.EntityUtil
import com.paragon.api.util.player.EntityFakePlayer
import com.paragon.api.util.render.RenderUtil.drawBorder
import com.paragon.api.util.render.RenderUtil.drawRect
import com.paragon.api.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.api.util.render.font.FontUtil.getHeight
import com.paragon.api.util.render.font.FontUtil.getStringWidth
import com.paragon.mixins.accessor.IRenderManager
import com.paragon.bus.listener.Listener
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemTool
import net.minecraft.util.text.TextFormatting
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * @author Surge
 */
object Nametags : Module("Nametags", Category.RENDER, "Draws nametags above players") {

    // Render settings
    private val health = Setting(
        "Health",
        true
    ) describedBy "Render the player's health"

    private val ping = Setting(
        "Ping",
        true
    ) describedBy "Render the player's ping"

    private val pops = Setting(
        "Pops",
        true
    ) describedBy "Render the player's totem pop count"

    private val armour = Setting(
        "Armour",
        true
    ) describedBy "Render the player's armour"

    private val armourDurability = Setting(
        "Durability",
        true
    ) describedBy "Render the player's armour durability" subOf armour

    // Scaling
    private val scaleFactor = Setting(
        "Scale",
        0.2f,
        0.1f,
        1f,
        0.1f
    ) describedBy "The scale of the nametag"

    private val distanceScale = Setting(
        "DistanceScale",
        true
    ) describedBy "Scale the nametag based on your distance from the player"

    private val outline = Setting(
        "Outline",
        true
    ) describedBy "Render the nametag outline"

    private val outlineWidth = Setting(
        "Width",
        0.5f,
        0.1f,
        2f,
        0.01f
    ) describedBy "The width of the outline" subOf outline

    private val outlineColour = Setting(
        "Colour",
        Color(185, 17, 255)
    ) describedBy "The colour of the outline" subOf outline

    override fun onRender3D() {
        // Prevent null pointer exceptions
        if (minecraft.anyNull || minecraft.player.connection == null || minecraft.player.ticksExisted < 20) {
            return
        }

        // Iterate through loaded players
        for (player in minecraft.world.playerEntities) {
            // Check the player isn't us
            if (player === minecraft.player) {
                continue
            }

            // Get render x, y, and z
            val renderValues = doubleArrayOf(
                (minecraft.renderManager as IRenderManager).renderX,
                (minecraft.renderManager as IRenderManager).renderY,
                (minecraft.renderManager as IRenderManager).renderZ
            )
            // Get player interpolated position
            val renderVec = EntityUtil.getInterpolatedPosition(player)

            // Get scale
            val distance = minecraft.player.getDistance(renderVec.x, renderVec.y, renderVec.z)
            var scale = scaleFactor.value * 5 / 50f
            if (distanceScale.value) {
                scale = (max((scaleFactor.value * 5).toDouble(), scaleFactor.value * distance) / 50).toFloat()
            }

            // Translate, rotate, and scale
            GL11.glPushMatrix()
            RenderHelper.enableStandardItemLighting()
            GlStateManager.disableLighting()
            GL11.glTranslated(
                renderVec.x - renderValues[0],
                renderVec.y + player.height + 0.1 + (if (player.isSneaking) 0.05 else 0.08) - renderValues[1],
                renderVec.z - renderValues[2]
            )
            GL11.glRotated(-minecraft.renderManager.playerViewY.toDouble(), 0.0, 1.0, 0.0)
            GL11.glRotated(
                minecraft.renderManager.playerViewX.toDouble(),
                (if (minecraft.gameSettings.thirdPersonView == 2) -1 else 1).toDouble(),
                0.0,
                0.0
            )
            GL11.glScaled(-scale.toDouble(), -scale.toDouble(), scale.toDouble())

            // Disable depth so we can see the nametag through walls
            GlStateManager.disableDepth()
            val stringBuilder = StringBuilder(player.name)
            if (health.value) {
                stringBuilder.append(" ").append(EntityUtil.getTextColourFromEntityHealth(player))
                    .append(EntityUtil.getEntityHealth(player).roundToInt())
            }
            if (ping.value && minecraft.connection != null) {
                minecraft.connection!!.getPlayerInfo(player.uniqueID)
                stringBuilder.append(" ")
                    .append(getPingColour(minecraft.connection!!.getPlayerInfo(player.uniqueID).responseTime)).append(
                        minecraft.connection!!.getPlayerInfo(player.uniqueID).responseTime
                    )
            }
            if (pops.value) {
                stringBuilder.append(" ").append(TextFormatting.GOLD).append("-")
                    .append(if (player is EntityFakePlayer) 0 else Paragon.INSTANCE.popManager.getPops(player))
            }

            // Get nametag width
            val width = getStringWidth(stringBuilder.toString())

            // Center nametag
            GL11.glTranslated((-width / 2).toDouble(), -20.0, 0.0)

            // Draw background
            drawRect(0f, 0f, width, getHeight() + if (isEnabled) 0 else 2, -0x70000000)

            // Draw border
            if (outline.value) {
                drawBorder(
                    0f,
                    0f,
                    width,
                    getHeight() + if (isEnabled) 0 else 2,
                    outlineWidth.value,
                    outlineColour.value.rgb
                )
            }

            // Render string
            drawStringWithShadow(stringBuilder.toString(), 2f, 2f, -1)

            // Render armour
            if (armour.value) {
                // Get the items we want to render
                val stacks = ArrayList<ItemStack>()
                stacks.add(player.heldItemMainhand)
                Collections.reverse(player.inventory.armorInventory)
                stacks.addAll(player.inventory.armorInventory)
                Collections.reverse(player.inventory.armorInventory)
                stacks.add(player.heldItemOffhand)

                // Get armour count
                var count = 0
                for (stack in stacks) {
                    if (stack.isEmpty) {
                        continue
                    }
                    count++
                }

                // Center armour
                var armourX = (width / 2 - count * 18 / 2).toInt()

                // Render armour
                for (stack in stacks) {
                    // Check the stack isn't empty
                    if (stack.item !== Items.AIR) {
                        // Y value
                        val y = -20
                        GL11.glPushMatrix()
                        GL11.glDepthMask(true)
                        GlStateManager.clear(256)
                        GlStateManager.disableDepth()
                        GlStateManager.enableDepth()
                        RenderHelper.enableStandardItemLighting()
                        minecraft.renderItem.zLevel = -100.0f
                        GlStateManager.scale(1f, 1f, 0.01f)

                        // Render the armour
                        minecraft.renderItem.renderItemAndEffectIntoGUI(stack, armourX, y)
                        minecraft.renderItem.renderItemOverlays(minecraft.fontRenderer, stack, armourX, y)
                        minecraft.renderItem.zLevel = 0.0f
                        GlStateManager.scale(1f, 1f, 1f)
                        RenderHelper.disableStandardItemLighting()
                        GlStateManager.enableAlpha()
                        GlStateManager.disableBlend()
                        GlStateManager.disableLighting()
                        GlStateManager.scale(0.5, 0.5, 0.5)
                        GlStateManager.disableDepth()
                        val itemInfo: MutableList<String> = ArrayList()

                        // Render the armour's durability
                        if (armourDurability.value && stack.item is ItemArmor || stack.item is ItemSword || stack.item is ItemTool) {
                            val damage = 1 - stack.itemDamage.toFloat() / stack.maxDamage.toFloat()
                            itemInfo.add((damage * 100).toInt().toString() + "%")
                        }

                        // Render enchants
                        val enchants = stack.enchantmentTagList
                        for (i in 0 until enchants.tagCount()) {
                            // Get enchant ID and level
                            val id = enchants.getCompoundTagAt(i).getInteger("id")
                            val level = enchants.getCompoundTagAt(i).getInteger("lvl")

                            // Get the enchantment
                            val enchantment = Enchantment.getEnchantmentByID(id)

                            // Make sure the enchantment is valid
                            if (enchantment != null) {
                                // Don't render if it's a curse
                                if (enchantment.isCurse) {
                                    continue
                                }

                                // Get enchantment name
                                val enchantmentName =
                                    enchantment.getTranslatedName(level).substring(0, 4) + if (level == 1) "" else level

                                // Render the enchantment's name and level
                                itemInfo.add(enchantmentName)
                            }
                        }
                        var yOffset = -(itemInfo.size * getHeight()) + 15
                        for (info in itemInfo) {
                            var colour = -1
                            if (info == itemInfo[0]) {
                                val green =
                                    (stack.maxDamage.toFloat() - stack.itemDamage.toFloat()) / stack.maxDamage.toFloat()
                                val red = 1 - green
                                colour = Color(red, green, 0f, 1f).rgb
                            }
                            drawStringWithShadow(info, (armourX * 2 + 4).toFloat(), y + yOffset, colour)
                            yOffset += getHeight()
                        }

                        // Re enable depth
                        GlStateManager.enableDepth()
                        GlStateManager.scale(2.0f, 2.0f, 2.0f)
                        GL11.glPopMatrix()

                        // Increase X value
                        armourX += 18
                    }
                }
            }

            // End render
            GlStateManager.enableDepth()
            GlStateManager.disableBlend()
            GL11.glPopMatrix()
        }
    }

    private fun getPingColour(ping: Int): TextFormatting {
        return when {
            ping < 0 -> TextFormatting.RED
            ping < 50 -> TextFormatting.GREEN
            ping < 150 -> TextFormatting.YELLOW
            else -> TextFormatting.RED
        }
    }

    @Listener
    fun onRenderNametag(event: RenderNametagEvent) {
        if (event.entity is EntityPlayer) {
            event.cancel()
        }
    }

}