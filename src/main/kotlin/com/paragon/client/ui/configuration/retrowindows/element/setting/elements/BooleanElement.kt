package com.paragon.client.ui.configuration.retrowindows.element.setting.elements

import com.paragon.Paragon
import com.paragon.api.event.client.SettingUpdateEvent
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.font.FontUtil
import com.paragon.client.systems.module.impl.client.ClickGUI
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.retrowindows.element.module.ModuleElement
import com.paragon.client.ui.configuration.retrowindows.element.setting.SettingElement
import com.paragon.client.ui.util.Click
import me.surge.animation.Animation
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11.glScalef
import java.awt.Color

/**
 * @author Surge
 */
class BooleanElement(parent: ModuleElement, setting: Setting<Boolean>, x: Float, y: Float, width: Float, height: Float) : SettingElement<Boolean>(parent, setting, x, y, width, height) {

    val enabled = Animation(ClickGUI.animationSpeed::value, setting.value, ClickGUI.easing::value)

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        enabled.state = setting.value

        RenderUtil.drawRect(x + 3, y + 3, width - 4, height - 4, Color(100, 100, 100).rgb)
        RenderUtil.drawRect(x + 2, y + 2, width - 4, height - 4, Color(130, 130, 130).rgb)

        RenderUtil.drawHorizontalGradientRect(
            x + 2,
            y + 2,
            ((width - 4) * enabled.getAnimationFactor()).toFloat(),
            height - 4,
            Colours.mainColour.value.rgb,
            if (ClickGUI.gradient.value) Colours.mainColour.value.brighter().brighter().rgb else Colours.mainColour.value.rgb
        )

        glScalef(0.8f, 0.8f, 0.8f)

        val scaleFactor = 1 / 0.8f
        FontUtil.drawStringWithShadow(setting.name, (x + 5) * scaleFactor, (y + 5f) * scaleFactor, -1)

        glScalef(scaleFactor, scaleFactor, scaleFactor)

        val scissorY = MathHelper.clamp(y, parent.parent.y + parent.parent.height, (parent.parent.y + parent.parent.height + parent.parent.scissorHeight) - getTotalHeight())
        val scissorHeight = getTotalHeight()

        if (expanded.getAnimationFactor() > 0) {
            var yOffset = 0f

            RenderUtil.pushScissor(x.toDouble(), scissorY.toDouble(), width.toDouble(), scissorHeight.toDouble())

            subSettings.forEach {
                if (it.setting.isVisible()) {
                    it.x = x + 2
                    it.y = y + height + yOffset

                    it.draw(mouseX, mouseY, mouseDelta)

                    yOffset += it.getTotalHeight()
                }
            }

            RenderUtil.popScissor()
        }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (isHovered(mouseX, mouseY) && y in parent.parent.y + parent.parent.height..parent.parent.y + parent.parent.height + parent.parent.scissorHeight) {
            if (click == Click.LEFT) {
                setting.setValue(!setting.value)
                Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
            } else if (click == Click.RIGHT) {
                expanded.state = !expanded.state
            }
        }
    }

}