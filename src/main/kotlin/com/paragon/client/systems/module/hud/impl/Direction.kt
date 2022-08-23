@file:Suppress("SuspiciousVarProperty")

package com.paragon.client.systems.module.hud.impl

import com.paragon.api.util.player.PlayerUtil
import com.paragon.api.util.render.font.FontUtil
import com.paragon.api.util.string.StringUtil
import com.paragon.client.systems.module.hud.HUDModule
import com.paragon.client.systems.module.impl.client.Colours
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 */
object Direction : HUDModule("Direction", "Displays what direction you are facing") {

    override fun render() {
        FontUtil.drawStringWithShadow(
            "Direction " + TextFormatting.WHITE + StringUtil.getFormattedText(PlayerUtil.direction) + " [" + PlayerUtil.getAxis(PlayerUtil.direction) + "]",
            x,
            y,
            Colours.mainColour.value.rgb
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