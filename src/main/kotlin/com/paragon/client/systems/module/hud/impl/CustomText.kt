@file:Suppress("SuspiciousVarProperty")

package com.paragon.client.systems.module.hud.impl

import com.paragon.api.setting.Setting
import com.paragon.api.util.render.font.FontUtil
import com.paragon.client.systems.module.hud.HUDModule
import java.awt.Color

/**
 * @author Surge
 */
object CustomText : HUDModule("CustomText", "Display custom text of your choice!") {

    private val text = Setting(
        "Text",
        "Paragon on top!"
    ) describedBy "The text to display"

    private val textColour = Setting(
        "TextColour",
        Color.WHITE
    ) describedBy "The colour of the text"

    override fun render() {
        FontUtil.drawStringWithShadow(text.value, x, y, textColour.value.rgb)
    }

    override var width = FontUtil.getStringWidth(text.value)
        get() = FontUtil.getStringWidth(text.value)

    override var height = FontUtil.getHeight()
        get() = FontUtil.getHeight()

}