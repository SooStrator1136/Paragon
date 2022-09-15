package com.paragon.impl.ui.configuration.paragon

import com.paragon.util.render.BlurUtil
import com.paragon.util.render.font.FontUtil
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.ui.configuration.GuiImplementation
import com.paragon.impl.ui.configuration.paragon.panel.CategoryPanel
import com.paragon.impl.ui.configuration.shared.Panel
import com.paragon.impl.ui.util.Click
import com.paragon.impl.module.Category
import com.paragon.util.string.StringUtil

/**
 * @author Surge
 * @since 06/08/2022
 */
object ParagonGUI : GuiImplementation() {

    private val panels = ArrayList<Panel>()

    init {
        var x = 20f

        Category.values().forEach {
            panels.add(CategoryPanel(it, x, 20f, 95f, 20f))

            x += 100f
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, mouseDelta: Int) {
        var tooltipName = ""
        var tooltipContent = ""

        // Render the last window in the list over the others etc
        panels.reverse()

        panels.forEach { panel ->
            panel.draw(mouseX.toFloat(), mouseY.toFloat(), mouseDelta)

            if (panel is CategoryPanel && panel.tooltipName.isNotEmpty()) {
                tooltipName = panel.tooltipName
                tooltipContent = panel.tooltipContent
            }
        }

        panels.reverse()

        if (tooltipName.isNotEmpty() && tooltipContent.isNotEmpty()) {
            val text = StringUtil.wrap(tooltipContent, 30)
            val width = FontUtil.getStringWidth(text) + 6f
            val height = 18f + (text.split(System.lineSeparator()).size * FontUtil.getHeight())

            BlurUtil.blur(mouseX + 8, mouseY + 8, width.toInt(), height.toInt(), ClickGUI.intensity.value.toInt())

            FontUtil.drawStringWithShadow(tooltipName, mouseX + 9f, mouseY + 12f, -1)
            FontUtil.drawStringWithShadow(text, mouseX + 9f, mouseY + 12f + FontUtil.getHeight(), -1)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        panels.forEach { panel ->
            panel.mouseClicked(mouseX.toFloat(), mouseY.toFloat(), Click.getClick(mouseButton))
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        panels.forEach { panel ->
            panel.mouseReleased(mouseX.toFloat(), mouseY.toFloat(), Click.getClick(mouseButton))
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        panels.forEach { panel ->
            panel.keyTyped(typedChar, keyCode)
        }
    }

}