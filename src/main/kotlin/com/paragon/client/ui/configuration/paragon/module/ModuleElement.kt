package com.paragon.client.ui.configuration.paragon.module

import com.paragon.api.module.Module
import com.paragon.api.setting.Bind
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.ColourUtil.fade
import com.paragon.api.util.render.ColourUtil.integrateAlpha
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.font.FontUtil
import com.paragon.client.systems.module.impl.client.ClickGUI
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.paragon.panel.CategoryPanel
import com.paragon.client.ui.configuration.paragon.setting.SettingElement
import com.paragon.client.ui.configuration.paragon.setting.impl.*
import com.paragon.client.ui.configuration.shared.RawElement
import com.paragon.client.ui.util.Click
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.util.math.MathHelper
import java.awt.Color

/**
 * @author Surge
 * @since 06/08/2022
 */
class ModuleElement(val module: Module, val panel: CategoryPanel, x: Float, y: Float, width: Float, height: Float) : RawElement(x, y, width, height) {

    private val hover: Animation = Animation({ 100f }, false, { Easing.LINEAR })
    private val enabled: Animation = Animation({ 200f }, module.isEnabled, { Easing.LINEAR })

    val subElements = ArrayList<SettingElement<*>>()
    val expanded = Animation(ClickGUI.animationSpeed::value, false, ClickGUI.easing::value)

    init {
        module.settings.forEach {
            when (it.value) {
                is Boolean -> subElements.add(BooleanElement(it as Setting<Boolean>, this, x, y, width, height))
                is Number -> subElements.add(SliderElement(it as Setting<Number>, this, x, y, width, height))
                is Enum<*> -> subElements.add(EnumElement(it as Setting<Enum<*>>, this, x, y, width, height))
                is Color -> subElements.add(ColourElement(it as Setting<Color>, this, x, y, width, height))
                is Bind -> subElements.add(BindElement(it as Setting<Bind>, this, x, y, width, height))
                is String -> subElements.add(StringElement(it as Setting<String>, this, x, y, width, height))
            }
        }
    }

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        hover.state = isHovered(mouseX, mouseY)
        enabled.state = module.isEnabled

        RenderUtil.drawRect(x, y, width, height, Color(53, 53, 74).fade(Color(64, 64, 92), hover.getAnimationFactor()).rgb)

        RenderUtil.scaleTo(x + 5, y + 7, 0f, 0.6, 0.6, 0.6) {
            if (hover.getAnimationFactor() > 0.5) {
                FontUtil.drawStringWithShadow(if (module.isEnabled) "Enabled" else "Disabled", x + 5, y + 7 + (5 * hover.getAnimationFactor()).toFloat(), Color(255, 0, 0).fade(Color(0, 255, 0), enabled.getAnimationFactor()).integrateAlpha(MathHelper.clamp(255 * hover.getAnimationFactor(), 5.0, 255.0).toFloat()).rgb)
            }
        }

        RenderUtil.scaleTo(x + 5, y + 5, 0f, 0.8, 0.8, 0.8) {
            FontUtil.drawStringWithShadow(module.name, x + 5, y + 5 - (3 * hover.getAnimationFactor()).toFloat(), Color.GRAY.brighter().fade(Color.WHITE, enabled.getAnimationFactor()).rgb)
        }

        RenderUtil.drawRect(x + width - 7, y, 7f, height, Color(37, 42, 51, 100).rgb)

        // lel
        FontUtil.defaultFont.drawStringWithShadow(".", x + width - 6, y - 5, -1)
        FontUtil.defaultFont.drawStringWithShadow(".", x + width - 6, y - 1, -1)
        FontUtil.defaultFont.drawStringWithShadow(".", x + width - 6, y + 3, -1)

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

            RenderUtil.drawRect(x, y + height, 1f, offset - y - height, Colours.mainColour.value.rgb)
        }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        if (expanded.state) {
            subElements.forEach {
                if (it.setting.isVisible()) {
                    it.mouseClicked(mouseX, mouseY, click)
                }
            }
        }

        if (isHovered(mouseX, mouseY)) {
            if (click == Click.LEFT) {
                module.toggle()
            }

            else if (click == Click.RIGHT) {
                expanded.state = !expanded.state
            }
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {
        if (expanded.state) {
            subElements.forEach {
                if (it.setting.isVisible()) {
                    it.mouseReleased(mouseX, mouseY, click)
                }
            }
        }
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        if (expanded.state) {
            subElements.forEach {
                if (it.setting.isVisible()) {
                    it.keyTyped(character, keyCode)
                }
            }
        }
    }

    fun getAbsoluteHeight(): Float {
        return height + subElements.filter { it.setting.isVisible() }.sumOf { it.getAbsoluteHeight().toDouble() }.toFloat() * expanded.getAnimationFactor().toFloat()
    }

    override fun isHovered(mouseX: Float, mouseY: Float): Boolean {
        return super.isHovered(mouseX, mouseY) && mouseY > panel.y + panel.height && mouseY < panel.y + panel.height + 240.0
    }

}