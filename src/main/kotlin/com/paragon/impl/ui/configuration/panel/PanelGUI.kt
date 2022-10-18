package com.paragon.impl.ui.configuration.panel

import com.paragon.impl.module.Category
import com.paragon.impl.module.client.ClientFont
import com.paragon.impl.ui.configuration.GuiImplementation
import com.paragon.impl.ui.configuration.panel.impl.CategoryPanel
import com.paragon.impl.ui.configuration.panel.impl.setting.BindElement
import com.paragon.impl.ui.configuration.panel.impl.setting.StringElement
import com.paragon.impl.ui.util.Click
import com.paragon.util.calculations.Timer
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.max

class PanelGUI : GuiImplementation() {

    val panels = arrayListOf<CategoryPanel>()

    private val searchAnimation = Animation(400f, false, Easing.CUBIC_IN_OUT)
    var search = ""

    init {
        var x = 30f

        Category.values().forEach {
            panels.add(CategoryPanel(this, it, x, 25f, 90f, 22f))
            x += 95f
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, mouseDelta: Int) {
        panels.reversed().forEach {
            it.draw(mouseX.toFloat(), mouseY.toFloat(), mouseDelta)
        }

        Keyboard.enableRepeatEvents(true)

        searchAnimation.state = search.isNotEmpty()

        val rectWidth = max(12 + FontUtil.getStringWidth("Search") * 1.5, FontUtil.getStringWidth(search).toDouble() + 12).toFloat()

        RenderUtil.drawRoundedRect((width / 2f) - (rectWidth / 2), ScaledResolution(Minecraft.getMinecraft()).scaledHeight - (73 * searchAnimation.getAnimationFactor()).toFloat(), rectWidth, 37f, 2f, Color(50, 50, 50, (150 * searchAnimation.getAnimationFactor()).toInt()))
        FontUtil.drawCenteredString(search, width / 2f, ScaledResolution(Minecraft.getMinecraft()).scaledHeight - (50 * searchAnimation.getAnimationFactor()).toFloat(), Color.WHITE, true)

        RenderUtil.scaleTo(width / 2f, ScaledResolution(Minecraft.getMinecraft()).scaledHeight - (75 * searchAnimation.getAnimationFactor()).toFloat(), 0f, 1.5, 1.5, 0.0) {
            FontUtil.drawCenteredString("Search", width / 2f, ScaledResolution(Minecraft.getMinecraft()).scaledHeight - (69 * searchAnimation.getAnimationFactor()).toFloat(), Color.WHITE, true)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        panels.reversed().forEach {
            it.mouseClicked(mouseX.toFloat(), mouseY.toFloat(), Click.getClick(mouseButton))
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        panels.reversed().forEach {
            it.mouseReleased(mouseX.toFloat(), mouseY.toFloat(), Click.getClick(mouseButton))
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        // fetch these before as [RawElement.keyTyped()] in the [StringElement] and [BindElement] objects will cause [StringElement.listening.state] and [BindElement.listening.state] to be set to false
        // i have no idea if im doing inline markup right lmao
        val setting = panels.any { it.elements.any { moduleElement -> moduleElement.elements.any { setting -> setting is StringElement && setting.listening.state || setting is BindElement && setting.listening.state }}}
        val sub = panels.any { it.elements.any { moduleElement -> moduleElement.elements.any { setting -> setting.elements.any { sub -> sub is StringElement && sub.listening.state || setting is BindElement && setting.listening.state }}}}

        panels.reversed().forEach {
            it.keyTyped(typedChar, keyCode)
        }

        if (!setting && !sub) {
            if (keyCode == Keyboard.KEY_BACK) {
                if (search.isNotEmpty()) {
                    search = search.substring(0, search.length - 1)
                }
            }

            else if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                search += typedChar
            }
        }
    }

}