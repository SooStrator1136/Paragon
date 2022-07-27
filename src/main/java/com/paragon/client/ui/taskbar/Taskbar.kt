package com.paragon.client.ui.taskbar

import com.paragon.api.util.Wrapper
import com.paragon.api.util.render.ITextRenderer
import com.paragon.api.util.render.RenderUtil
import com.paragon.client.systems.module.impl.client.ClickGUI
import com.paragon.client.systems.module.impl.client.ClientFont
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.console.ConsoleGUI
import com.paragon.client.ui.util.animation.Animation
import net.minecraft.client.gui.ScaledResolution
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.awt.Color

/**
 * @author Surge
 */
@SideOnly(Side.CLIENT)
class Taskbar : Wrapper, ITextRenderer {

    private val icons: Array<Icon>

    private val tooltipAnimation = Animation({ 200f }, false, ClickGUI.easing::value)
    var tooltip = ""
        set(value) = if (value == "") {
            tooltipAnimation.state = false
        } else {
            field = value
            tooltipAnimation.state = true
        }

    init {
        val x = getStringWidth("Paragon") + 10F
        icons = arrayOf(
            Icon("GUI", x) { ClickGUI.getGUI() },
            Icon("Console", x + getStringWidth("GUI") + 7) { ConsoleGUI() }
        )
    }

    fun drawTaskbar(mouseX: Int, mouseY: Int) {
        val scaledResolution = ScaledResolution(Wrapper.mc)
        RenderUtil.drawRect(
            0f,
            (scaledResolution.scaledHeight - 18).toFloat(),
            scaledResolution.scaledWidth.toFloat(),
            18f,
            Color(20, 20, 20).rgb
        )
        RenderUtil.drawRect(
            0f,
            (scaledResolution.scaledHeight - 19).toFloat(),
            scaledResolution.scaledWidth.toFloat(),
            2f,
            Colours.mainColour.value.rgb
        )
        renderText(
            "Paragon",
            2f,
            (scaledResolution.scaledHeight - if (ClientFont.isEnabled) 12 else 11).toFloat(),
            Colours.mainColour.value.rgb
        )

        for (icon in icons) {
            icon.draw(mouseX, mouseY)
        }

        if (tooltip !== "" && ClickGUI.tooltips.value) {
            renderText(
                tooltip,
                (scaledResolution.scaledWidth - (getStringWidth(tooltip) + 2) * tooltipAnimation.getAnimationFactor()).toFloat(),
                (scaledResolution.scaledHeight - if (ClientFont.isEnabled) 12 else 11).toFloat(),
                -1
            )
        }
    }

    fun mouseClicked(mouseX: Int, mouseY: Int) {
        for (icon in icons) {
            icon.whenClicked(mouseX, mouseY)
        }
    }

}