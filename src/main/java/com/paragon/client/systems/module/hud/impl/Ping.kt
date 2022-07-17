package com.paragon.client.systems.module.hud.impl

import com.paragon.client.systems.module.hud.HUDModule
import com.paragon.client.systems.module.impl.client.Colours
import net.minecraft.util.text.TextFormatting

/**
 * @author Wolfsurge
 */
object Ping : HUDModule("Ping", "Displays your ping in ms") {

    override fun render() {
        renderText("Ping: " + TextFormatting.WHITE + getPing() + "ms", x, y, Colours.mainColour.value.rgb)
    }

    override fun getWidth() = getStringWidth("Ping: " + getPing() + "ms")

    override fun getHeight() = fontHeight

    fun getPing() = if (mc.connection != null) minecraft.connection!!.getPlayerInfo(mc.session.profile.id).responseTime else -1

}