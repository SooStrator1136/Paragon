package com.paragon.impl.ui.configuration.paragon.setting.impl

import com.paragon.Paragon
import com.paragon.impl.event.client.SettingUpdateEvent
import com.paragon.impl.setting.Setting
import com.paragon.util.render.ColourUtil.fade
import com.paragon.util.render.font.FontUtil
import com.paragon.impl.ui.configuration.paragon.module.ModuleElement
import com.paragon.impl.ui.configuration.paragon.setting.SettingElement
import com.paragon.impl.ui.util.Click
import com.paragon.util.render.RenderUtil
import com.paragon.util.string.StringUtil
import java.awt.Color

/**
 * @author Surge
 * @since 06/08/2022
 */
class EnumElement(setting: Setting<Enum<*>>, module: ModuleElement, x: Float, y: Float, width: Float, height: Float) : SettingElement<Enum<*>>(setting, module, x, y, width, height) {

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        RenderUtil.drawRect(x, y, width, height, Color(53, 53, 74).fade(Color(64, 64, 92), hover.getAnimationFactor()).rgb)

        RenderUtil.scaleTo(x + 5, y + 7, 0f, 0.5, 0.5, 0.5) {
            if (hover.getAnimationFactor() > 0.5) {
                FontUtil.drawStringWithShadow("Next: " + StringUtil.getFormattedText(setting.nextMode), x + 5, y + 7 + (7 * hover.getAnimationFactor()).toFloat(), Color.GRAY.rgb)
            }
        }

        RenderUtil.scaleTo(x + width - FontUtil.getStringWidth(StringUtil.getFormattedText(setting.value)), y + 5, 0f, 0.7, 0.7, 0.7) {
            val side: Float = (x + width - 9 - FontUtil.getStringWidth(StringUtil.getFormattedText(setting.value)) * 0.7f)

            FontUtil.drawStringWithShadow(StringUtil.getFormattedText(setting.value), side, y + 5, Color.GRAY.brighter().fade(Color.GRAY.brighter().brighter(), hover.getAnimationFactor()).rgb)
        }

        RenderUtil.scaleTo(x + 5, y + 5, 0f, 0.7, 0.7, 0.7) {
            FontUtil.drawStringWithShadow(setting.name, x + 5, y + 5 - (3 * hover.getAnimationFactor()).toFloat(), -1)
        }

        super.draw(mouseX, mouseY, mouseDelta)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (click == Click.LEFT && hover.state) {
            setting.setValue(setting.nextMode)
            Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
        }
    }

}