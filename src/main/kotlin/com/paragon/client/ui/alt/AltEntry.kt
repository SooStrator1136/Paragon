package com.paragon.client.ui.alt

import com.paragon.api.util.Wrapper
import com.paragon.api.util.render.RenderUtil.drawRect
import com.paragon.api.util.render.font.FontUtil.renderCenteredString
import com.paragon.client.managers.alt.Alt

class AltEntry(val alt: Alt, var offset: Float) : Wrapper {

    fun drawAlt(mouseX: Int, mouseY: Int, screenWidth: Int) {
        drawRect(0f, offset, screenWidth.toFloat(), 20f, if (AltManagerGUI.selectedAltEntry === this) -0x6aeeeeef else -0x6b000000)
        renderCenteredString(alt.email, screenWidth / 2f, offset + 10, -1, true)
    }

    fun clicked(mouseX: Int, mouseY: Int, screenWidth: Int) {
        if (isHovered(0f, offset, screenWidth.toFloat(), 20f, mouseX, mouseY)) {
            alt.login()
        }
    }

}