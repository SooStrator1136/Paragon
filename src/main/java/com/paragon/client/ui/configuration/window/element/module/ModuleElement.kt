package com.paragon.client.ui.configuration.window.element.module

import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.ColourUtil
import com.paragon.api.util.render.RenderUtil
import com.paragon.client.systems.module.impl.client.ClickGUI
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.window.element.Element
import com.paragon.client.ui.configuration.window.element.setting.impl.BooleanElement
import com.paragon.client.ui.configuration.window.element.setting.impl.SliderElement
import com.paragon.client.ui.configuration.window.window.Window
import com.paragon.client.ui.util.Click
import com.paragon.client.ui.util.animation.Animation
import com.paragon.client.ui.util.animation.Easing
import java.awt.Color

/**
 * @author Wolfsurge
 */
class ModuleElement(val module: Module, window: Window, x: Float, y: Float, width: Float, height: Float) : Element(window, x, y, width, height) {

    private val enabled: Animation = Animation({ ClickGUI.animationSpeed.value }, module.isEnabled, { ClickGUI.easing.value })
    val flash: Animation = Animation({ 1250f }, false, { Easing.LINEAR })

    init {
        module.getSettings().forEach {
            if (it.value is Boolean) {
                subElements.add(BooleanElement(it as Setting<Boolean>, window, x, y, width, height))
            }

            else if (it.value is Number) {
                subElements.add(SliderElement(it as Setting<Number>, window, x, y, width, height + 4))
            }
        }
    }

    override fun draw(mouseX: Int, mouseY: Int) {
        super.draw(mouseX, mouseY)

        enabled.state = module.isEnabled

        RenderUtil.drawRect(x, y, width, 16f, Color((75 + (20 * hoverAnimation.getAnimationFactor())).toInt(), (75 + (20 * hoverAnimation.getAnimationFactor())).toInt(), (75 + (20 * hoverAnimation.getAnimationFactor())).toInt()).rgb)
        RenderUtil.drawRect(x, y, (width * enabled.getAnimationFactor()).toFloat(), 16f, ColourUtil.integrateAlpha(Colours.mainColour.value, (255 - (55 * hoverAnimation.getAnimationFactor())).toFloat()).rgb)

        if (flash.state && flash.getAnimationFactor() == 1.0) {
            flash.state = false
        }

        RenderUtil.drawRect(x, y, width, height, ColourUtil.integrateAlpha(Color.WHITE, (255 * flash.getAnimationFactor()).toFloat()).rgb)

        renderText(module.name, x + 2, y + 5f, -1)

        if (expandAnimation.getAnimationFactor() > 0) {
            var y = this.y + height

            subElements.forEach {
                it.updatePosition(x, y)

                it.draw(mouseX, mouseY)

                y += it.height
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Click) {
        if (isHovered(mouseX, mouseY)) {
            if (button == Click.LEFT) {
                module.toggle()
            }

            else if (button == Click.RIGHT) {
                expandAnimation.state = !expandAnimation.state
            }
        }

        super.mouseClicked(mouseX, mouseY, button)
    }

    override fun getTotalHeight(): Float {
        var subHeight = 0f

        subElements.forEach { subHeight += it.getTotalHeight() }

        return height + (subHeight * expandAnimation.getAnimationFactor()).toFloat()
    }

}