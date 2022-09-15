package com.paragon.impl.ui.taskbar.start


import com.paragon.util.render.font.FontUtil
import com.paragon.util.render.RenderUtil
import java.awt.Color

/**
 * @author Surge
 * @since 27/07/2022
 */
class StartElement(
    val name: String, private val onAction: () -> Unit, var x: Float, var y: Float, var width: Float, var height: Float
) {

    fun draw(mouseX: Int, mouseY: Int) {
        val hovered = mouseX.toFloat() in x..x + width && mouseY.toFloat() in y..y + height

        RenderUtil.drawRect(
            x + 2, y + 2, width - 2, height - 2, Color(100, 100, 100).rgb
        )
        RenderUtil.drawRect(
            x + 1, y + 1, width - 2, height - 2, Color(120 - (if (hovered) 10 else 0), 120 - (if (hovered) 10 else 0), 120 - (if (hovered) 10 else 0)).rgb
        )

        FontUtil.drawStringWithShadow(name, x + 3, y + 4, -1)
    }

    fun clicked() {
        onAction()
    }

}