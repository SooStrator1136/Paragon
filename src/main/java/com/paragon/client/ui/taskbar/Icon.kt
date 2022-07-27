package com.paragon.client.ui.taskbar

import com.paragon.api.util.Wrapper
import com.paragon.api.util.render.ColourUtil
import com.paragon.api.util.render.ITextRenderer
import com.paragon.client.systems.module.impl.client.ClientFont
import com.paragon.client.systems.module.impl.client.Colours
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.function.Supplier

/**
 * @author Surge
 */
@SideOnly(Side.CLIENT)
class Icon(val name: String, val x: Float, private val whenClicked: Supplier<GuiScreen>) : Wrapper, ITextRenderer {

    private var y = 0F

    fun draw(mouseX: Int, mouseY: Int) {
        val scaledResolution = ScaledResolution(minecraft)
        y = scaledResolution.scaledHeight - 16.5f

        ColourUtil.setColour(-1)
        renderCenteredString(
            name,
            x + (getStringWidth(name) + 6) / 2,
            y + if (ClientFont.isEnabled) 2 else 4,
            if (isHovered(
                    x,
                    y,
                    getStringWidth(name) + 6,
                    18f,
                    mouseX,
                    mouseY
                )
            ) Colours.mainColour.value.rgb else -1,
            false
        )
    }

    fun whenClicked(mouseX: Int, mouseY: Int) {
        if (isHovered(x.toFloat(), y, getStringWidth(name) + 6, 16f, mouseX, mouseY)) {
            minecraft.displayGuiScreen(whenClicked.get())
        }
    }

}