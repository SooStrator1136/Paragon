package com.paragon.client.ui.taskbar

import com.paragon.api.util.Wrapper
import com.paragon.api.util.render.RenderUtil
import com.paragon.client.ui.taskbar.start.StartMenu
import com.paragon.client.ui.util.Click
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

/**
 * @author Surge
 * @since 26/07/2022
 */
object Taskbar : Wrapper {

    var tooltip: String = ""

    private val startMenu = StartMenu(5f, 5f, 60f, 20f)

    fun draw(mouseX: Int, mouseY: Int) {
        val scaledResolution = ScaledResolution(minecraft)

        RenderUtil.drawRect(0f, scaledResolution.scaledHeight - 26f, scaledResolution.scaledWidth.toFloat(), 26f, Color(148, 148, 148).rgb)

        startMenu.x = 2f
        startMenu.y = scaledResolution.scaledHeight - 23f
        startMenu.draw(mouseX, mouseY)
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, click: Click) {
        startMenu.mouseClicked(mouseX, mouseY, click)
    }

}