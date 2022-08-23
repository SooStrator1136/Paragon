package com.paragon.client.systems.module.hud.impl

import com.paragon.api.util.render.RenderUtil.drawBorder
import com.paragon.api.util.render.RenderUtil.drawRect
import com.paragon.api.util.render.RenderUtil.renderItemStack
import com.paragon.client.systems.module.hud.HUDModule
import com.paragon.client.systems.module.impl.client.Colours
import net.minecraft.item.ItemStack

/**
 * @author Surge
 */
object Inventory : HUDModule("Inventory", "Displays the contents of your inventory") {

    override fun render() {
        drawRect(x, y, width, height - 4, -0x70000000)
        drawBorder(x, y, width, height - 4, 1f, Colours.mainColour.value.rgb)

        var x = 0f
        var y = 0f

        for (i in 9..35) {
            val stack: ItemStack = minecraft.player.inventory.getStackInSlot(i)

            renderItemStack(stack, this.x + x, this.y + y, true)

            x += 18f

            // cba for calcs
            if (i == 17 || i == 26 || i == 35) {
                x = 0f
                y += 18f
            }
        }
    }

    override var width: Float
        get() = (18 * 9).toFloat()

        set(width) {
            super.width = width
        }

    override var height: Float
        get() = (18 * 3 + 4).toFloat()

        set(height) {
            super.height = height
        }

}