@file:Suppress("SuspiciousVarProperty")

package com.paragon.impl.module.hud.impl

import com.paragon.util.render.font.FontUtil
import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.module.client.Colours
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 */
object Ping : HUDModule("Ping", "Displays your ping in ms") {

    override fun render() {
        FontUtil.drawStringWithShadow("Ping: " + TextFormatting.WHITE + getPing() + "ms", x, y, Colours.mainColour.value.rgb)
    }

    override var width = FontUtil.getStringWidth("Ping: " + getPing() + "ms")
        get() = FontUtil.getStringWidth("Ping: " + getPing() + "ms")

    override var height = FontUtil.getHeight()
        get() = FontUtil.getHeight()

    fun getPing(): Int = if (minecraft.connection != null && minecraft.connection!!.getPlayerInfo(minecraft.player.uniqueID) != null) {
        minecraft.connection!!.getPlayerInfo(minecraft.player.uniqueID).responseTime
    }
    else {
        -1
    }


}