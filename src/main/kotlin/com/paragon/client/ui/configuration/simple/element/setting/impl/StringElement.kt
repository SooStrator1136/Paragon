package com.paragon.client.ui.configuration.simple.element.setting.impl

import com.paragon.Paragon
import com.paragon.api.event.client.SettingUpdateEvent
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.ColourUtil.integrateAlpha
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.font.FontUtil
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.simple.element.setting.SettingElement
import com.paragon.client.ui.util.Click
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.glScalef
import java.awt.Color

/**
 * @author Surge
 * @since 31/07/2022
 */
class StringElement(setting: Setting<String>, x: Float, y: Float, width: Float, height: Float) : SettingElement<String>(setting, x, y, width, height) {

    private var listening = false

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        val hovered = isHovered(mouseX, mouseY)

        if (hovered && !listening) {
            RenderUtil.drawRect(x, y, width, height, Color(200, 200, 200, 150).rgb)
        }

        if (listening) {
            RenderUtil.drawRect(x, y, width, height, Colours.mainColour.value.integrateAlpha(if (hovered) 205f else 150f).rgb)
        }

        glScalef(0.85f, 0.85f, 0.85f).let {
            val factor = 1 / 0.85f

            FontUtil.drawStringWithShadow(setting.name, (x + 4) * factor, (y + 4) * factor, -1)

            val valueX = (x + width - FontUtil.getStringWidth(setting.value) * 0.85f - 3) * factor
            FontUtil.drawStringWithShadow(setting.value, valueX, (y + 4f) * factor, Color(190, 190, 190).rgb)

            glScalef(factor, factor, factor)
        }

        super.draw(mouseX, mouseY, mouseDelta)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (isHovered(mouseX, mouseY) && click == Click.LEFT) {
            listening = !listening
        }
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        super.keyTyped(character, keyCode)

        if (listening) {
            if (keyCode == Keyboard.KEY_BACK) {
                if (setting.value.isNotEmpty()) {
                    setting.setValue(setting.value.substring(0, setting.value.length - 1))
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                }
            } else if (keyCode == Keyboard.KEY_RETURN) {
                listening = false
            } else if (ChatAllowedCharacters.isAllowedCharacter(character)) {
                setting.setValue(setting.value + character)
                Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
            }
        }
    }

}