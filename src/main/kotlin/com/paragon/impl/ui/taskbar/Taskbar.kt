package com.paragon.impl.ui.taskbar

import com.paragon.util.render.font.FontUtil
import com.paragon.impl.module.client.Colours
import com.paragon.impl.ui.taskbar.start.StartMenu
import com.paragon.impl.ui.util.Click
import com.paragon.util.Wrapper
import com.paragon.util.render.RenderUtil
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

/**
 * @author Surge
 * @since 26/07/2022
 */
object Taskbar : Wrapper {

    var tooltip = ""

    val expandAnimation: Animation = Animation({ 500f }, false, { Easing.CUBIC_IN_OUT })
    val timer: Animation = Animation({ 1000f }, false, { Easing.LINEAR })

    fun draw(mouseX: Int, mouseY: Int) {
        val scaledResolution = ScaledResolution(minecraft)
        val hovered = mouseY.toFloat() in scaledResolution.scaledHeight - 80f..scaledResolution.scaledHeight.toFloat()

        var state = false

        if (StartMenu.expandAnimation.state || hovered) {
            state = true
        }

        expandAnimation.state = state

        RenderUtil.drawRect(
            0f, scaledResolution.scaledHeight - (26f * expandAnimation.getAnimationFactor().toFloat()), scaledResolution.scaledWidth.toFloat(), 26f, Color(148, 148, 148).rgb
        )

        StartMenu.x = 2f
        StartMenu.y = scaledResolution.scaledHeight - (23f * expandAnimation.getAnimationFactor().toFloat())
        StartMenu.draw(mouseX, mouseY)

        RenderUtil.drawRect(
            150f, scaledResolution.scaledHeight - (26f * expandAnimation.getAnimationFactor().toFloat()) - 2, scaledResolution.scaledWidth.toFloat() - 150f, 2f, Colours.mainColour.value.rgb
        )

        if (tooltip != "") {
            RenderUtil.drawRect(
                (scaledResolution.scaledWidth - FontUtil.getStringWidth(tooltip) - 4) + ((FontUtil.getStringWidth(
                    tooltip
                ) + 4) * expandAnimation.getAnimationFactor()).toFloat(), scaledResolution.scaledHeight - 16f, FontUtil.getStringWidth(tooltip) + 4, 13f, 0x90000000.toInt()
            )
            FontUtil.drawStringWithShadow(
                tooltip, scaledResolution.scaledWidth - FontUtil.getStringWidth(tooltip) - 2, scaledResolution.scaledHeight - 14f, Color.WHITE.rgb
            )
        }
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, click: Click) {
        if (!StartMenu.mouseClicked(mouseX, mouseY, click)) {
            StartMenu.expandAnimation.state = false
        }
    }

}