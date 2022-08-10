package com.paragon.client.systems.module.hud.impl

import com.paragon.api.util.render.RenderUtil.drawBorder
import com.paragon.api.util.render.RenderUtil.drawRect
import com.paragon.api.util.render.RenderUtil.renderItemStack
import com.paragon.client.systems.module.hud.HUDModule
import com.paragon.client.systems.module.impl.client.Colours
import net.minecraft.init.Items
import net.minecraft.item.ItemStack

/**
 * @author Surge, SooStrator1136
 */
object Totems : HUDModule("Totems", "Displays the amount of totems in your inventory") {

    override fun render() {
        drawRect(x, y, width, height, 0x70000000)
        drawBorder(x, y, width, height, 1f, Colours.mainColour.value.rgb)
        renderItemStack(ItemStack(Items.TOTEM_OF_UNDYING, getTotemAmount()), x + 1, y + 2, true)
    }

    override var width = 19F

    override var height = 19F

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
