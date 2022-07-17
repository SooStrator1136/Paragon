package com.paragon.client.systems.module.hud.impl

import com.paragon.client.systems.module.hud.HUDModule
import com.paragon.client.systems.module.impl.client.Colours
import net.minecraft.client.Minecraft
import net.minecraft.util.text.TextFormatting

/**
 * @author Wolfsurge
 */
object FPS : HUDModule("FPS", "Renders your FPS on screen") {

    override fun render() {
        renderText(getText(), x, y, Colours.mainColour.value.rgb)
    }

    override fun getWidth() = getStringWidth(getText())

    override fun getHeight() = fontHeight

    private fun getText() = "FPS " + TextFormatting.WHITE + Minecraft.getDebugFPS()

}