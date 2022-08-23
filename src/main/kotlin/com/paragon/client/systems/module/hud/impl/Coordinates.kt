package com.paragon.client.systems.module.hud.impl

import com.paragon.api.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.api.util.render.font.FontUtil.getHeight
import com.paragon.api.util.render.font.FontUtil.getStringWidth
import com.paragon.client.systems.module.hud.HUDModule
import com.paragon.client.systems.module.impl.client.Colours
import net.minecraft.util.text.TextFormatting
import kotlin.math.roundToInt

object Coordinates : HUDModule("Coordinates", "Displays your coordinates") {

    override fun render() {
        drawStringWithShadow(text, x, y, Colours.mainColour.value.rgb)
    }

    override var width: Float
        get() = getStringWidth(text)

        set(width) {
            super.width = width
        }

    override var height: Float
        get() = getHeight()

        set(height) {
            super.height = height
        }

    val text: String
        get() = "X " + TextFormatting.WHITE + minecraft.player.posX.roundToInt() + TextFormatting.RESET + " Y " + TextFormatting.WHITE + minecraft.player.posY.roundToInt() + TextFormatting.RESET + " Z " + TextFormatting.WHITE + minecraft.player.posZ.roundToInt()

}