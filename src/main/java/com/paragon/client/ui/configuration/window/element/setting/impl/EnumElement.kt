package com.paragon.client.ui.configuration.window.element.setting.impl

import com.paragon.api.setting.Setting
import com.paragon.api.util.render.ColourUtil
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.string.StringUtil
import com.paragon.client.systems.module.impl.client.ClickGUI
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.window.element.setting.SettingElement
import com.paragon.client.ui.configuration.window.window.Window
import com.paragon.client.ui.util.Click
import com.paragon.client.ui.util.animation.Animation
import java.awt.Color

/**
 * @author Wolfsurge
 */
class EnumElement(setting: Setting<Enum<*>>, window: Window, x: Float, y: Float, width: Float, height: Float) : SettingElement<Enum<*>>(setting, window, x, y, width, height) {

    override fun draw(mouseX: Int, mouseY: Int) {
        RenderUtil.drawRect(x, y, width, 16f, Color((75 + (20 * hoverAnimation.getAnimationFactor())).toInt(), (75 + (20 * hoverAnimation.getAnimationFactor())).toInt(), (75 + (20 * hoverAnimation.getAnimationFactor())).toInt()).rgb)

        renderText(setting.name, x + 2, y + 5f, -1)
        renderText(StringUtil.getFormattedText(setting.value), x + width - getStringWidth(StringUtil.getFormattedText(setting.value)) - 3, y + 5f, Color(185, 185, 190).rgb)

        super.draw(mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Click) {
        super.mouseClicked(mouseX, mouseY, button)

        if (isHovered(mouseX, mouseY)) {
            if (button == Click.LEFT) {
                setting.setValue(setting.nextMode)
            }

            else if (button == Click.RIGHT) {
                expandAnimation.state = !expandAnimation.state
            }
        }
    }

}