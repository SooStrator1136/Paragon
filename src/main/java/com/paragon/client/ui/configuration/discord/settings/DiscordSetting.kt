package com.paragon.client.ui.configuration.discord.settings

import com.paragon.api.setting.Setting
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.RenderUtil.scaleTo
import com.paragon.api.util.render.font.FontUtil
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.discord.GuiDiscord
import com.paragon.client.ui.configuration.discord.IRenderable
import org.lwjgl.util.Rectangle
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author SooStrator1136
 */
abstract class DiscordSetting(val dSetting: Setting<*>) : IRenderable {

    val bounds = Rectangle()
    val msgStyleHeight = FontUtil.getHeight() + 1F

    override fun render(mouseX: Int, mouseY: Int) {
        if (bounds.contains(mouseX, mouseY)) {
            RenderUtil.drawRect(
                bounds.x.toFloat(),
                bounds.y.toFloat(),
                bounds.width.toFloat(),
                bounds.height.toFloat(),
                GuiDiscord.MSG_HOVERED.rgb
            )
        }

        FontUtil.drawStringWithShadow(
            dSetting.name,
            bounds.x.toFloat(),
            bounds.y.toFloat(),
            Colours.mainColour.value.rgb
        )

        //Render time in 12h format
        run {
            val dateX = bounds.x + FontUtil.getStringWidth(dSetting.name) + 2F
            val dateY = bounds.y + (FontUtil.getHeight() / 2F)
            val scaleFac = (FontUtil.getHeight() / 2.0) / FontUtil.getHeight()
            scaleTo(dateX, dateY, 0F, scaleFac, scaleFac, 1.0) {
                FontUtil.drawStringWithShadow(
                    SimpleDateFormat("hh:mm a").format(Date(System.currentTimeMillis())),
                    dateX,
                    dateY,
                    Colours.mainColour.value.rgb
                )
            }
        }
    }

}