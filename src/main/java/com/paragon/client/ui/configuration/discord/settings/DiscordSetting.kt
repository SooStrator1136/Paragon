package com.paragon.client.ui.configuration.discord.settings

import com.paragon.api.setting.Setting
import com.paragon.api.util.render.ITextRenderer
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.discord.IRenderable
import org.lwjgl.opengl.GL11.*
import org.lwjgl.util.Rectangle
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author SooStrator1136
 */
abstract class DiscordSetting(private val dSetting: Setting<*>) : IRenderable, ITextRenderer {

    val bounds = Rectangle()
    val msgStyleHeight = fontHeight

    override fun render(mouseX: Int, mouseY: Int) {
        renderText(dSetting.name, bounds.x.toFloat(), bounds.y.toFloat(), Colours.mainColour.value.rgb)

        val dateX = bounds.x + getStringWidth(dSetting.name) + 2F
        val dateY = bounds.y + (fontHeight / 2F)
        glPushMatrix()
        glTranslatef(dateX, dateY, 0F)
        val scaleFac = ((fontHeight - 1F) / 2.0) / fontHeight
        glScaled(scaleFac, scaleFac, 1.0)
        glTranslatef(-dateX, -dateY, 0F)
        renderText(
            SimpleDateFormat("hh:mm a").format(Date(System.currentTimeMillis())),
            dateX,
            dateY,
            Colours.mainColour.value.rgb
        )
        glPopMatrix()
    }

}