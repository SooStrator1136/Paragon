package com.paragon.client.systems.module.hud.impl

import com.paragon.api.util.player.PlayerUtil
import com.paragon.api.util.string.StringUtil
import com.paragon.client.systems.module.hud.HUDModule
import com.paragon.client.systems.module.impl.client.Colours
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 */
object Direction : HUDModule("Direction", "Displays what direction you are facing") {

    override fun render() {
        renderText(
            "Direction " + TextFormatting.WHITE + StringUtil.getFormattedText(PlayerUtil.getDirection()) + " [" + PlayerUtil.getAxis(PlayerUtil.getDirection()) + "]",
            x,
            y,
            Colours.mainColour.value.rgb
        )
    }

    override fun getWidth() = getStringWidth("Direction " + StringUtil.getFormattedText(PlayerUtil.getDirection()) + " [" + PlayerUtil.getAxis(PlayerUtil.getDirection()) + "]")

    override fun getHeight() = fontHeight

}