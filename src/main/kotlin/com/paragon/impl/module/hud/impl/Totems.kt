package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.client.Colours
import com.paragon.impl.module.hud.HUDModule
import com.paragon.util.render.ColourUtil.toColour
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.RenderUtil.drawBorder
import com.paragon.util.render.RenderUtil.drawRect
import com.paragon.util.render.RenderUtil.drawRoundedOutline
import com.paragon.util.render.RenderUtil.drawRoundedRect
import com.paragon.util.render.RenderUtil.renderItemStack
import com.paragon.util.render.font.FontUtil
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import java.awt.Color

/**
 * @author Surge, SooStrator1136
 */
object Totems : HUDModule("Totems", "Displays the amount of totems in your inventory") {

    override fun render() {
        drawRoundedRect(x, y, width, height, 5f, Color(0, 0, 0, 180))

        renderItemStack(ItemStack(Items.TOTEM_OF_UNDYING, getTotemAmount()), x + 2.5f, y + 2.5f, false)

        val totems = getTotemAmount()
        FontUtil.drawStringWithShadow(totems.toString(), x + width - FontUtil.getStringWidth(totems.toString()) - 3, y + height - FontUtil.getHeight() - 1, Color.WHITE)

        drawRoundedOutline(x, y, width, height, 5f, 1f, Colours.mainColour.value)
    }

    override var width = 21F

    override var height = 21F

    private fun getTotemAmount(): Int {
        var count = 0

        for (i in 0..35) {
            if (minecraft.player.inventory.getStackInSlot(i).item === Items.TOTEM_OF_UNDYING) {
                count++
            }
        }

        if (minecraft.player.heldItemMainhand.item === Items.TOTEM_OF_UNDYING) {
            count++
        }
        if (minecraft.player.heldItemOffhand.item === Items.TOTEM_OF_UNDYING) {
            count++
        }

        return count
    }

}
