package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.client.Colours
import com.paragon.impl.module.hud.HUDModule
import com.paragon.util.render.ColourUtil.toColour
import com.paragon.util.render.RenderUtil.drawBorder
import com.paragon.util.render.RenderUtil.drawRect
import com.paragon.util.render.RenderUtil.drawRoundedOutline
import com.paragon.util.render.RenderUtil.drawRoundedRect
import com.paragon.util.render.RenderUtil.renderItemStack
import net.minecraft.item.ItemStack
import java.awt.Color

/**
 * @author Surge
 */
object Inventory : HUDModule("Inventory", "Displays the contents of your inventory") {

    override fun render() {
        drawRoundedRect(x, y, width, height, 5f, Color(0, 0, 0, 180))

        var x = 2f
        var y = 2f

        for (i in 9..35) {
            val stack: ItemStack = minecraft.player.inventory.getStackInSlot(i)

            renderItemStack(stack, this.x + x, this.y + y, true)

            x += 19f

            if (i == 17 || i == 26 || i == 35) {
                x = 2f
                y += 19f
            }
        }

        drawRoundedOutline(this.x, this.y, width, height, 5f, 1f, Colours.mainColour.value)
    }

    override var width: Float
        get() = (19 * 9).toFloat() + 2f
        set(width) {
            super.width = width
        }

    override var height: Float
        get() = (19 * 3).toFloat() + 2f
        set(height) {
            super.height = height
        }

}