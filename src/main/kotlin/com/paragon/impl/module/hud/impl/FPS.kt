@file:Suppress("SuspiciousVarProperty")

package com.paragon.impl.module.hud.impl

import com.paragon.util.render.font.FontUtil
import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.module.client.Colours
import net.minecraft.client.Minecraft
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 */
object FPS : HUDModule("FPS", "Renders your FPS on screen") {

    override fun render() {
        FontUtil.drawStringWithShadow(getText(), x, y, Colours.mainColour.value.rgb)
    }

    override var width = FontUtil.getStringWidth(getText())
        get() = FontUtil.getStringWidth(getText())

    override var height = FontUtil.getHeight()
        get() = FontUtil.getHeight()

    private fun getText() = "FPS " + TextFormatting.WHITE + Minecraft.getDebugFPS()

}