package com.paragon.client.ui.configuration.paragon.setting.impl

import com.paragon.Paragon
import com.paragon.api.event.client.SettingUpdateEvent
import com.paragon.api.setting.Bind
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.ColourUtil.fade
import com.paragon.api.util.render.ColourUtil.integrateAlpha
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.font.FontUtil
import com.paragon.client.ui.configuration.paragon.module.ModuleElement
import com.paragon.client.ui.configuration.paragon.setting.SettingElement
import com.paragon.client.ui.util.Click
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.util.math.MathHelper
import org.lwjgl.input.Keyboard
import java.awt.Color

/**
 * @author Surge
 * @since 06/08/2022
 */
class BindElement(setting: Setting<Bind>, module: ModuleElement, x: Float, y: Float, width: Float, height: Float) : SettingElement<Bind>(setting, module, x, y, width, height) {

    private val listening: Animation = Animation({ 200f }, false, { Easing.LINEAR })

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        RenderUtil.drawRect(x, y, width, height, Color(53, 53, 74).fade(Color(64, 64, 92), hover.getAnimationFactor()).rgb)

        RenderUtil.scaleTo(x + 5, y + 7, 0f, 0.5, 0.5, 0.5) {
            if (hover.getAnimationFactor() > 0.5) {
                FontUtil.drawStringWithShadow(if (listening.state) "Listening" else "Not Listening", x + 5, y + 7 + (7 * hover.getAnimationFactor()).toFloat(), Color(255, 0, 0).fade(Color(0, 255, 0), listening.getAnimationFactor()).integrateAlpha(MathHelper.clamp(255 * hover.getAnimationFactor(), 5.0, 255.0).toFloat()).rgb)
            }
        }

        RenderUtil.scaleTo(x + width - FontUtil.getStringWidth(setting.value.getButtonName()), y + 3, 0f, 0.7, 0.7, 0.7) {
            val side: Float = (x + width - 9 - FontUtil.getStringWidth(setting.value.getButtonName()) * 0.7f)

            FontUtil.drawStringWithShadow(setting.value.getButtonName(), side, y + 5, Color.GRAY.brighter().fade(Color.GRAY.brighter().brighter(), hover.getAnimationFactor()).rgb)
        }

        RenderUtil.scaleTo(x + 5, y + 5, 0f, 0.7, 0.7, 0.7) {
            FontUtil.drawStringWithShadow(setting.name, x + 5, y + 5 - (3 * hover.getAnimationFactor()).toFloat(), Color.GRAY.brighter().fade(Color.WHITE, listening.getAnimationFactor()).rgb)
        }

        super.draw(mouseX, mouseY, mouseDelta)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (click == Click.LEFT && hover.state) {
            listening.state = !listening.state
            return
        }

        if (listening.state) {
            listening.state = false

            setting.value.buttonCode = click.button
            setting.value.device = Bind.Device.MOUSE
            Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
        }
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        super.keyTyped(character, keyCode)

        if (listening.state) {
            listening.state = false

            setting.value.buttonCode = if (keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK) 0 else keyCode
            setting.value.device = Bind.Device.KEYBOARD
            Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
        }
    }

}