package com.paragon.client.ui.configuration.window.element.setting.impl

import com.paragon.api.setting.Setting
import com.paragon.api.util.render.ColourUtil
import com.paragon.api.util.render.RenderUtil
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
class BooleanElement(setting: Setting<Boolean>, window: Window, x: Float, y: Float, width: Float, height: Float) : SettingElement<Boolean>(setting, window, x, y, width, height) {

    private val enabled: Animation = Animation({ ClickGUI.animationSpeed.value }, setting.value, { ClickGUI.easing.value })

    override fun draw(mouseX: Int, mouseY: Int) {
        enabled.state = setting.value
        super.draw(mouseX, mouseY)

        RenderUtil.drawRect(x, y, width, 16f, Color((75 + (20 * hoverAnimation.getAnimationFactor())).toInt(), (75 + (20 * hoverAnimation.getAnimationFactor())).toInt(), (75 + (20 * hoverAnimation.getAnimationFactor())).toInt()).rgb)
        RenderUtil.drawRect(x, y, (width * enabled.getAnimationFactor()).toFloat(), 16f, ColourUtil.integrateAlpha(Colours.mainColour.value, (255 - (55 * hoverAnimation.getAnimationFactor())).toFloat()).rgb)

        renderText(setting.name, x + 2, y + 5f, -1)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Click) {
        super.mouseClicked(mouseX, mouseY, button)

        if (isHovered(mouseX, mouseY)) {
            if (button == Click.LEFT) {
                setting.setValue(!setting.value)
            }
        }
    }

}