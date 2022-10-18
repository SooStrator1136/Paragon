package com.paragon.impl.ui.console

import com.paragon.Paragon
import com.paragon.util.render.font.FontUtil
import com.paragon.impl.module.client.Colours
import com.paragon.util.Wrapper
import com.paragon.util.render.RenderUtil
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.gui.ScaledResolution
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.input.Keyboard
import java.awt.Color

@SideOnly(Side.CLIENT)
class Console(private val title: String, val width: Float, val height: Float) : Wrapper {

    // List of lines
    private val lines: MutableList<String> = ArrayList(5)
    private var guiTextField: GuiTextField

    init {
        val scaledResolution = ScaledResolution(minecraft)
        guiTextField = GuiTextField(
            0, minecraft.fontRenderer, (scaledResolution.scaledWidth / 2 - width / 2).toInt() + 2, (scaledResolution.scaledHeight / 2 - height / 2 + height - 11).toInt(), width.toInt(), 11
        )
    }

    fun init() {
        val scaledResolution = ScaledResolution(minecraft)
        guiTextField = GuiTextField(
            0, minecraft.fontRenderer, (scaledResolution.scaledWidth / 2 - width / 2).toInt() + 3, (scaledResolution.scaledHeight / 2 - height / 2 + height - 13).toInt(), width.toInt() - 6, 11
        )
    }

    fun draw(mouseX: Int, mouseY: Int) {
        val scaledResolution = ScaledResolution(minecraft)

        val x = (scaledResolution.scaledWidth / 2f) - (width / 2f)
        val y = (scaledResolution.scaledHeight / 2f) - (height / 2f)

        RenderUtil.drawRoundedRect(x - 2, y - 2, width + 4, height + 4, 1.5f, Color(20, 20, 25))
        RenderUtil.drawRoundedOutline(x, y, width, height, 2f, 2f, Colours.mainColour.value)

        FontUtil.drawStringWithShadow(title, x + 5f, y + 5f, Color.WHITE)

        lines.reverse()

        RenderUtil.drawRect(x, y + 17.5f, width, 1f, Colours.mainColour.value)

        RenderUtil.pushScissor(
            (scaledResolution.scaledWidth / 2f) - (width / 2f), (scaledResolution.scaledHeight / 2f) - (height / 2f) + 20f, width, (height - 26.5f)
        )

        var lineY = (scaledResolution.scaledHeight / 2f) - (height / 2f) + height - 26

        for (string in lines) {
            FontUtil.drawStringWithShadow(string, (scaledResolution.scaledWidth / 2f) - (width / 2f) + 5, lineY, Color.WHITE)
            lineY -= 11
        }

        lines.reverse()

        RenderUtil.popScissor()

        if (!guiTextField.text.startsWith("> ")) {
            guiTextField.text = "> " + guiTextField.text
        }

        guiTextField.drawTextBox()
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        guiTextField.mouseClicked(mouseX, mouseY, mouseButton)
    }

    fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_RETURN) {
            Paragon.INSTANCE.commandManager.handleCommands(guiTextField.text.substring(2), true)
            guiTextField.text = ""
            guiTextField.isFocused = false
            return
        }
        guiTextField.textboxKeyTyped(typedChar, keyCode)
    }

    fun addLine(line: String) {
        lines.add(line)
    }

}