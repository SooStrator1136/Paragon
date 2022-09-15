package com.paragon.impl.ui.alt

import com.paragon.util.render.font.FontUtil.renderCenteredString
import com.paragon.impl.managers.alt.Alt
import com.paragon.util.Wrapper
import com.paragon.util.render.RenderUtil.drawRect

class AltEntry(val alt: Alt, var offset: Float) : Wrapper {

    fun drawAlt(screenWidth: Int) {
        drawRect(
            0f, offset, screenWidth.toFloat(), 20f, if (AltManagerGUI.selectedAltEntry === this) -0x6aeeeeef else -0x6b000000
        )
        renderCenteredString(alt.email, screenWidth / 2f, offset + 10, -1, true)
    }

    fun clicked(mouseX: Int, mouseY: Int, screenWidth: Int) {
        if (isHovered(0f, offset, screenWidth.toFloat(), 20f, mouseX, mouseY)) {
            alt.login()
        }
    }

}