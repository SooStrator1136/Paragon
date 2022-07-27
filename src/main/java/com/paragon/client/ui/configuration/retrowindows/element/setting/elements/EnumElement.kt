package com.paragon.client.ui.configuration.retrowindows.element.setting.elements

import com.paragon.api.setting.Setting
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.string.StringUtil
import com.paragon.client.systems.module.impl.client.ClickGUI
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.retrowindows.element.module.ModuleElement
import com.paragon.client.ui.configuration.retrowindows.element.setting.SettingElement
import com.paragon.client.ui.util.Click
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11.glScalef
import java.awt.Color

/**
 * @author Surge
 */
class EnumElement(parent: ModuleElement, setting: Setting<Enum<*>>, x: Float, y: Float, width: Float, height: Float) : SettingElement<Enum<*>>(parent, setting, x, y, width, height) {

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        RenderUtil.drawRect(x + 3, y + 3, width - 4, height - 4, Color(100, 100, 100).rgb)
        RenderUtil.drawRect(x + 2, y + 2, width - 4, height - 4, Color(130, 130, 130).rgb)

        RenderUtil.drawHorizontalGradientRect(x + 2, y + 2,  width - 4, height - 4, Colours.mainColour.value.rgb, if (ClickGUI.gradient.value) Colours.mainColour.value.brighter().brighter().rgb else Colours.mainColour.value.rgb)

        glScalef(0.8f, 0.8f, 0.8f)

        val scaleFactor = 1 / 0.8f
        renderText(setting.name, (x + 5) * scaleFactor, (y + 5f) * scaleFactor, -1)

        val valueX: Float = (x + width - getStringWidth(StringUtil.getFormattedText(setting.value)) * 0.8f - 5) * scaleFactor

        renderText(StringUtil.getFormattedText(setting.value), valueX, (y + 5f) * scaleFactor, Color(190, 190, 190).rgb)

        glScalef(scaleFactor, scaleFactor, scaleFactor)

        if (expanded.getAnimationFactor() > 0) {
            var yOffset = 0f

            val scissorY: Double = MathHelper.clamp(y + height, parent.parent.y + parent.parent.height, ((parent.parent.y + parent.parent.scissorHeight) - getSubsettingHeight() * expanded.getAnimationFactor().toFloat()) + height).toDouble()

            var scissorHeight: Double = MathHelper.clamp(getSubsettingHeight().toDouble() * expanded.getAnimationFactor(), 0.0, parent.parent.scissorHeight.toDouble())

            RenderUtil.pushScissor(x.toDouble(), scissorY, width.toDouble(), scissorHeight)

            subsettings.forEach {
                it.x = x + 2
                it.y = y + height + yOffset

                it.draw(mouseX, mouseY, mouseDelta)

                yOffset += it.getTotalHeight()
            }

            RenderUtil.popScissor()
        }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (isHovered(mouseX, mouseY) && y in parent.parent.y + parent.parent.height..parent.parent.y + parent.parent.height + parent.parent.scissorHeight) {
            if (click == Click.LEFT) {
                setting.setValue(setting.nextMode)
            } else if (click == Click.RIGHT) {
                expanded.state = !expanded.state
            }
        }
    }

}