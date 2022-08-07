package com.paragon.client.ui.configuration.simple

import com.paragon.api.module.Category
import com.paragon.client.ui.configuration.GuiImplementation
import com.paragon.client.ui.configuration.simple.panel.CategoryPanel
import com.paragon.client.ui.util.Click

/**
 * @author Surge
 * @since 31/07/2022
 */
object SimpleGUI : GuiImplementation() {

    private val panels: ArrayList<CategoryPanel> = ArrayList()

    init {
        var x = 20f

        Category.values().forEach { category ->
            panels.add(CategoryPanel(category, x, 20f, 88f, 14f))

            x += 90f
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, mouseDelta: Int) {
        if (mouseDelta != 0) {
            if (mouseDelta > 0) {
                panels.forEach {
                    if (it.y < 20f) {
                        it.y += 16f
                    }
                }
            } else {
                panels.forEach {
                    if (it.y + it.totalHeight > 19) {
                        it.y -= 16f
                    }
                }
            }
        }

        panels.forEach {
            it.draw(mouseX.toFloat(), mouseY.toFloat(), mouseDelta)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        panels.forEach { it.mouseClicked(mouseX.toFloat(), mouseY.toFloat(), Click.getClick(mouseButton)) }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        panels.forEach { it.mouseReleased(mouseX.toFloat(), mouseY.toFloat(), Click.getClick(mouseButton)) }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        panels.forEach { it.keyTyped(typedChar, keyCode) }
    }
}