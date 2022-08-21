package com.paragon.client.ui.configuration.paragon.setting.impl

import com.paragon.Paragon
import com.paragon.api.event.client.SettingUpdateEvent
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.ColourUtil.fade
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.font.FontUtil
import com.paragon.client.ui.configuration.paragon.module.ModuleElement
import com.paragon.client.ui.configuration.paragon.setting.SettingElement
import com.paragon.client.ui.util.Click
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import java.awt.Color

/**
 * @author Surge
 * @since 06/08/2022
 */
class StringElement(setting: Setting<String>, module: ModuleElement, x: Float, y: Float, width: Float, height: Float) : SettingElement<String>(setting, module, x, y, width, height) {

    private val listening: Animation = Animation({ 200f }, false, { Easing.LINEAR })

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        RenderUtil.drawRect(
            x,
            y,
            width,
            height,
            Color(53, 53, 74).fade(Color(64, 64, 92), hover.getAnimationFactor()).rgb
        )

        RenderUtil.scaleTo(x + 5, y + 7, 0f, 0.5, 0.5, 0.5) {
            if (hover.getAnimationFactor() > 0.5) {
                FontUtil.drawStringWithShadow(
                    setting.value + if (listening.state) "_" else "",
                    x + 5,
                    y + 7 + (7 * hover.getAnimationFactor()).toFloat(),
                    Color.GRAY.rgb
                )
            }
        }

        RenderUtil.scaleTo(x + 5, y + 5, 0f, 0.7, 0.7, 0.7) {
            FontUtil.drawStringWithShadow(
                setting.name,
                x + 5,
                y + 5 - (3 * hover.getAnimationFactor()).toFloat(),
                Color.GRAY.brighter().fade(Color.WHITE, listening.getAnimationFactor()).rgb
            )
        }

        super.draw(mouseX, mouseY, mouseDelta)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (hover.state) {
            if (click == Click.LEFT) {
                listening.state = !listening.state
            } else if (click == Click.RIGHT) {
                expanded.state = !expanded.state
            }
        }
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        super.keyTyped(character, keyCode)

        if (listening.state) {
            if (keyCode == Keyboard.KEY_BACK) {
                if (setting.value.isNotEmpty()) {
                    setting.setValue(setting.value.substring(0, setting.value.length - 1))
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                }
            } else if (keyCode == Keyboard.KEY_RETURN) {
                listening.state = false
            } else if (ChatAllowedCharacters.isAllowedCharacter(character)) {
                setting.setValue(setting.value + character)
                Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
            }
        }
    }

}