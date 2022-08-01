@file:Suppress("ReplaceNotNullAssertionWithElvisReturn")

package com.paragon.client.ui.configuration.discord.settings.impl

import com.paragon.api.setting.Setting
import com.paragon.api.util.calculations.MathsUtil.getPercent
import com.paragon.api.util.calculations.MathsUtil.getPercentOf
import com.paragon.api.util.calculations.MathsUtil.roundDouble
import com.paragon.api.util.calculations.MathsUtil.roundToIncrementation
import com.paragon.api.util.minus
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.font.FontUtil
import com.paragon.api.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.api.util.render.font.FontUtil.getStringWidth
import com.paragon.client.ui.configuration.discord.GuiDiscord
import com.paragon.client.ui.configuration.discord.settings.DiscordSetting
import com.paragon.client.ui.util.Click
import org.lwjgl.input.Mouse
import org.lwjgl.util.Rectangle
import java.math.BigDecimal
import kotlin.math.max
import kotlin.random.Random

/**
 * @author SooStrator1136
 */
class DiscordNumber(val setting: Setting<Number>) : DiscordSetting(setting) {

    var dragging = false

    private val sliderBounds = Rectangle()

    private val size = Random.nextInt(100, 999).toString() + "." + Random.nextInt(10, 99) + " KB"

    private val maxProgressInfoWidth = max(
        getStringWidth("${setting.min}/${setting.max}"),
        getStringWidth("${setting.max}/${setting.max}")
    )

    init {
        bounds.height = (msgStyleHeight + (FontUtil.getHeight() * 7)).toInt() + 2
    }

    override fun render(mouseX: Int, mouseY: Int) {
        super.render(mouseX, mouseY)

        val mediaRect = Rectangle(
            bounds.x,
            (bounds.y + msgStyleHeight).toInt(),
            (bounds.width * 0.75).toInt(),
            (FontUtil.getHeight() * 7).toInt()
        )
        val lowerMediaRect = Rectangle(
            bounds.x + 5,
            (mediaRect.y + (FontUtil.getHeight() * 4)).toInt(),
            mediaRect.width - 10,
            (FontUtil.getHeight() * 2).toInt()
        )

        //Render basic media background
        run {
            RenderUtil.drawRoundedRect(
                mediaRect.x.toDouble(),
                mediaRect.y.toDouble(),
                mediaRect.width.toDouble(),
                mediaRect.height.toDouble(),
                2.5,
                2.5,
                2.5,
                2.5,
                GuiDiscord.MEDIA_BACKGROUND.rgb
            )
            RenderUtil.drawRoundedOutline(
                mediaRect.x.toDouble(),
                mediaRect.y.toDouble(),
                mediaRect.width.toDouble(),
                mediaRect.height.toDouble(),
                2.5,
                2.5,
                2.5,
                2.5,
                2F,
                GuiDiscord.MEDIA_BACKGROUND_BORDER.rgb
            )

            drawStringWithShadow(
                "${setting.name}.mp3",
                mediaRect.x + 5F,
                mediaRect.y + FontUtil.getHeight(),
                GuiDiscord.MEDIA_TITLE.rgb
            )
            drawStringWithShadow(
                size,
                mediaRect.x + 5F,
                mediaRect.y + (FontUtil.getHeight() * 2F),
                GuiDiscord.MEDIA_SIZE.rgb
            )
        }

        //Render basic slider background
        run {
            RenderUtil.drawRoundedRect(
                lowerMediaRect.x.toDouble(),
                lowerMediaRect.y.toDouble(),
                lowerMediaRect.width.toDouble(),
                lowerMediaRect.height.toDouble(),
                2.5,
                2.5,
                2.5,
                2.5,
                GuiDiscord.MEDIA_PROGRESS_BACKGROUND.rgb
            )

            drawStringWithShadow(
                "${setting.value}/${setting.max}",
                lowerMediaRect.x + 5F,
                lowerMediaRect.y + (FontUtil.getHeight() / 2F),
                -1
            )

            sliderBounds.setBounds(
                (lowerMediaRect.x + maxProgressInfoWidth + 10).toInt(),
                (lowerMediaRect.y + (FontUtil.getHeight() / 2)).toInt(),
                ((lowerMediaRect.width - maxProgressInfoWidth) - 15).toInt() + 1,
                FontUtil.getHeight().toInt()
            )

            RenderUtil.drawRoundedRect(
                sliderBounds.x.toDouble(),
                sliderBounds.y.toDouble(),
                sliderBounds.width - 1.0,
                sliderBounds.height.toDouble(),
                2.0,
                2.0,
                2.0,
                2.0,
                GuiDiscord.MEDIA_PROGRESSBAR_BACKGROUND.rgb
            )
        }

        //Render the progress/value
        run {
            RenderUtil.drawRoundedRect(
                sliderBounds.x.toDouble(),
                sliderBounds.y.toDouble(),
                getPercentOf(
                    getPercent(
                        (setting.value - setting.min!!).toDouble(),
                        (setting.max!! - setting.min!!).toDouble()
                    ), sliderBounds.width - 1.0
                ),
                sliderBounds.height.toDouble(),
                2.0,
                2.0,
                2.0,
                2.0,
                GuiDiscord.MEDIA_PROGRESS.rgb
            )
        }

        //Set new value if left mouse button is pressed
        if (Mouse.isButtonDown(Click.LEFT.button) && sliderBounds.contains(mouseX, mouseY)) {
            setting.setValue(getNewValue(mouseX))
        }
    }

    override fun onClick(mouseX: Int, mouseY: Int, button: Int) {
        if (!sliderBounds.contains(mouseX, mouseY) || button != Click.LEFT.button) {
            return
        }

        setting.setValue(getNewValue(mouseX))
    }

    private fun getNewValue(mouseX: Int) = roundToIncrementation(
        setting.incrementation!!.toDouble(),
        roundDouble(
            getPercentOf(
                getPercent(
                    (mouseX - sliderBounds.x).toDouble(),
                    sliderBounds.width - 1.0
                ),
                (setting.max!! - setting.min!!).toDouble(),
            ),
            BigDecimal.valueOf(setting.incrementation!!.toDouble()).scale()
        )
    )

    override fun onKey(keyCode: Int) {}

}