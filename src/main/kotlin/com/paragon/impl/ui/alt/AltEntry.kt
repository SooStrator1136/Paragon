package com.paragon.impl.ui.alt

import com.paragon.util.render.font.FontUtil.drawCenteredString
import com.paragon.impl.managers.alt.Alt
import com.paragon.util.Wrapper
import com.paragon.util.render.RenderUtil.drawRect
import java.awt.Color

class AltEntry(val alt: Alt, var offset: Float) : Wrapper {

    fun drawAlt(screenWidth: Int) {
        drawRect(
            0f,
            offset,
            screenWidth.toFloat(),
            20f,
            if (AltManagerGUI.selectedAltEntry === this) Color(238, 238, 239, 150) else Color(0, 0, 0, 150)
        )

        drawCenteredString(alt.email, screenWidth / 2f, offset + 10, Color.WHITE, true)
    }

    fun clicked(mouseX: Int, mouseY: Int, screenWidth: Int) {
        if (isHovered(0f, offset, screenWidth.toFloat(), 20f, mouseX, mouseY)) {
            alt.login()
        }
    }

}