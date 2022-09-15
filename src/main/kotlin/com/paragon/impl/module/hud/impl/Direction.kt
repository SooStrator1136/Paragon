@file:Suppress("SuspiciousVarProperty")

package com.paragon.impl.module.hud.impl

import com.paragon.util.player.PlayerUtil
import com.paragon.util.render.font.FontUtil
import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.module.client.Colours
import com.paragon.util.string.StringUtil
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 */
object Direction : HUDModule("Direction", "Displays what direction you are facing") {

    override fun render() {
        FontUtil.drawStringWithShadow(
            "Direction " + TextFormatting.WHITE + StringUtil.getFormattedText(PlayerUtil.direction) + " [" + PlayerUtil.getAxis(PlayerUtil.direction) + "]", x, y, Colours.mainColour.value.rgb
        )
    }

    override var width: Float = 0.0F
        get() = FontUtil.getStringWidth(
            "Direction " + StringUtil.getFormattedText(PlayerUtil.direction) + " [" + PlayerUtil.getAxis(
                PlayerUtil.direction
            ) + "]"
        )

    override var height = FontUtil.getHeight()
        get() = FontUtil.getHeight()

}