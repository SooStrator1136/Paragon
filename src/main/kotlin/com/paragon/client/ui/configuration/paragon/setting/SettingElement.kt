package com.paragon.client.ui.configuration.paragon.setting

import com.paragon.api.setting.Bind
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.font.FontUtil
import com.paragon.client.systems.module.impl.client.ClickGUI
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.paragon.module.ModuleElement
import com.paragon.client.ui.configuration.paragon.setting.impl.*
import com.paragon.client.ui.configuration.shared.RawElement
import com.paragon.client.ui.util.Click
import me.surge.animation.Animation
import me.surge.animation.Easing
import java.awt.Color

/**
 * @author Surge
 * @since 06/08/2022
 */
open class SettingElement<T>(val setting: Setting<T>, val module: ModuleElement, x: Float, y: Float, width: Float, height: Float) : RawElement(x, y, width, height) {

    val subElements = ArrayList<SettingElement<*>>()

    val hover = Animation({ 100f }, false, { Easing.LINEAR })
    val expanded = Animation(ClickGUI.animationSpeed::value, false, ClickGUI.easing::value)

    init {
        setting.subsettings.forEach {
            when (it.value) {
                is Boolean -> subElements.add(BooleanElement(it as Setting<Boolean>, module, x, y, width, height))
                is Number -> subElements.add(SliderElement(it as Setting<Number>, module, x, y, width, height))
                is Enum<*> -> subElements.add(EnumElement(it as Setting<Enum<*>>, module, x, y, width, height))
                is Color -> subElements.add(ColourElement(it as Setting<Color>, module, x, y, width, height))
                is Bind -> subElements.add(SettingElement(it as Setting<Bind>, module, x, y, width, height))
                is Setting<*> -> subElements.add(StringElement(it as Setting<String>, module, x, y, width, height))
            }
        }
    }

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        hover.state = isHovered(mouseX, mouseY)

        if (subElements.any { it.setting.isVisible() }) {
            RenderUtil.drawRect(x + width - 7, y, 7f, height, Color(37, 42, 51, 100).rgb)

            // lel
            FontUtil.defaultFont.drawStringWithShadow(".", x + width - 6, y - 5, -1)
            FontUtil.defaultFont.drawStringWithShadow(".", x + width - 6, y - 1, -1)
            FontUtil.defaultFont.drawStringWithShadow(".", x + width - 6, y + 3, -1)
        }

        if (expanded.getAnimationFactor() > 0) {
            var offset = y + height

            subElements.forEach {
                if (it.setting.isVisible()) {
                    it.x = x
                    it.y = offset

                    it.draw(mouseX, mouseY, mouseDelta)

                    offset += it.getAbsoluteHeight()
                }
            }

            RenderUtil.drawRect(x + 1, y + height, 1f, offset - y - height, Colours.mainColour.value.rgb)
        }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (hover.state && click == Click.RIGHT) {
            expanded.state = !expanded.state
        }

        if (expanded.state) {
            subElements.forEach {
                if (it.setting.isVisible()) {
                    it.mouseClicked(mouseX, mouseY, click)
                }
            }
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseReleased(mouseX, mouseY, click)

        if (expanded.state) {
            subElements.forEach {
                if (it.setting.isVisible()) {
                    it.mouseReleased(mouseX, mouseY, click)
                }
            }
        }
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        super.keyTyped(character, keyCode)

        if (expanded.state) {
            subElements.forEach {
                if (it.setting.isVisible()) {
                    it.keyTyped(character, keyCode)
                }
            }
        }
    }

    open fun getAbsoluteHeight(): Float {
        return height + (subElements.filter { it.setting.isVisible() }.sumOf { it.getAbsoluteHeight().toDouble() } * expanded.getAnimationFactor()).toFloat()
    }

    override fun isHovered(mouseX: Float, mouseY: Float): Boolean {
        return super.isHovered(mouseX, mouseY) && mouseY > module.panel.y + module.panel.height && mouseY < module.panel.y + module.panel.height + 240.0
    }

}