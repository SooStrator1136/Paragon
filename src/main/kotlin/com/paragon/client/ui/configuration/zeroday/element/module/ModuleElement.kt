package com.paragon.client.ui.configuration.zeroday.element.module

import com.paragon.api.module.Module
import com.paragon.api.setting.Bind
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.RenderUtil.drawRect
import com.paragon.api.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.api.util.render.font.FontUtil.getStringWidth
import com.paragon.client.ui.configuration.zeroday.element.Element
import com.paragon.client.ui.configuration.zeroday.element.setting.*
import com.paragon.client.ui.configuration.zeroday.panel.CategoryPanel
import com.paragon.client.ui.util.Click
import me.surge.animation.Animation
import me.surge.animation.Easing
import java.awt.Color
import java.util.function.Consumer

class ModuleElement(parent: CategoryPanel, module: Module, x: Float, y: Float, width: Float, height: Float) : Element(0, x, y, width, height) {
    val module: Module
    private val enabledAnimation = Animation({ 200f }, false) { Easing.LINEAR }

    init {
        this.parent = parent
        this.module = module

        module.settings.forEach(Consumer { setting: Setting<*> ->
            if (setting.value is Boolean) {
                subElements.add(BooleanElement(1, setting as Setting<Boolean?>, this, x, y, width, height))
            }

            else if (setting.value is Enum<*>) {
                subElements.add(EnumElement(1, setting as Setting<Enum<*>?>, this, x, y, width, height))
            }

            else if (setting.value is Number) {
                subElements.add(SliderElement(1, setting as Setting<Number?>, this, x, y, width, height))
            }

            else if (setting.value is Bind) {
                subElements.add(BindElement(1, setting as Setting<Bind?>, this, x, y, width, height))
            }

            else if (setting.value is Color) {
                subElements.add(ColourElement(1, setting as Setting<Color>, this, x, y, width, height))
            }

            else if (setting.value is String) {
                subElements.add(StringElement(1, setting as Setting<String?>, this, x, y, width, height))
            }
        })
    }

    override fun render(mouseX: Int, mouseY: Int, dWheel: Int) {
        enabledAnimation.state = module.isEnabled
        drawRect(x, y, width, getTotalHeight(), Color(40, 40, 45).rgb)
        drawRect(x, y, width, height, Color((40 + 30 * hover.getAnimationFactor()).toInt(), (40 + 30 * hover.getAnimationFactor()).toInt(), (45 + 30 * hover.getAnimationFactor()).toInt()).rgb)

        drawRect(x, y, (width * enabledAnimation.getAnimationFactor()).toFloat(), height, Color(Color.HSBtoRGB(parent!!.leftHue / 360, 1f, (0.75f + 0.25f * hover.getAnimationFactor()).toFloat())).rgb)

        val factor = (155 + 100 * enabledAnimation.getAnimationFactor()).toInt()
        val textColour = Color(factor, factor, factor)
        drawStringWithShadow(module.name, x + 5, y + height / 2 - 3.5f, textColour.rgb)

        if (subElements.size > 2) {
            drawStringWithShadow("...", x + width - getStringWidth("...") - 5, y + 2f, -1)
        }

        super.render(mouseX, mouseY, dWheel)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, click: Click) {
        if (isHovered(mouseX, mouseY) && parent!!.isElementVisible(this) && click == Click.LEFT) {
            module.toggle()
        }
        super.mouseClicked(mouseX, mouseY, click)
    }

}