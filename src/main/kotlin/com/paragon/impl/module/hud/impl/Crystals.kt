package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.client.Colours
import com.paragon.impl.module.hud.HUDModule
import com.paragon.util.render.ColourUtil.toColour
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.RenderUtil.drawBorder
import com.paragon.util.render.RenderUtil.drawRect
import com.paragon.util.render.RenderUtil.renderItemStack
import com.paragon.util.render.font.FontUtil
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.math.MathHelper
import java.awt.Color

object Crystals : HUDModule("Crystals", "Displays the amount of crystals in your inventory") {

    override fun render() {
        RenderUtil.drawRoundedRect(x, y, width, height, 5f, Color(0, 0, 0, 180))

        val itemStack = ItemStack(Items.END_CRYSTAL, crystals)
        renderItemStack(itemStack, x + width - 18, y + 2.5f, false)

        val crystals = crystals
        FontUtil.drawStringWithShadow(crystals.toString(), x + width - FontUtil.getStringWidth(crystals.toString()) - 3, y + height - FontUtil.getHeight() - 1, Color.WHITE)

        RenderUtil.drawRoundedOutline(x, y, width, height, 5f, 1f, Colours.mainColour.value)
    }

    override var width: Float
        get() = (FontUtil.getStringWidth(crystals.toString()) + 5).coerceAtLeast(21f)
        set(width) {
            super.width = width
        }

    override var height: Float
        get() = 21f
        set(height) {
            super.height = height
        }

    val crystals: Int
        get() {
            var count = 0

            for (i in 0..35) {
                val itemStack: ItemStack = minecraft.player.inventory.getStackInSlot(i)

                if (itemStack.item === Items.END_CRYSTAL) {
                    count += itemStack.count
                }
            }

            if (minecraft.player.heldItemMainhand.item === Items.END_CRYSTAL) {
                count += minecraft.player.heldItemMainhand.count
            }

            if (minecraft.player.heldItemOffhand.item === Items.END_CRYSTAL) {
                count += minecraft.player.heldItemOffhand.count
            }

            return count
        }

}