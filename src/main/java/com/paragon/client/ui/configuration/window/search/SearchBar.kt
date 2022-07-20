package com.paragon.client.ui.configuration.window.search

import com.paragon.Paragon
import com.paragon.api.util.render.ITextRenderer
import com.paragon.api.util.render.RenderUtil
import com.paragon.client.ui.configuration.window.window.Window
import com.paragon.client.ui.util.Click
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import java.awt.Color

/**
 * @author Wolfsurge
 */
class SearchBar(val window: Window, var x: Float, var y: Float, var width: Float, var height: Float) : ITextRenderer {

    var currentInput: String = ""
    var listening: Boolean = false
    var searchElements = arrayListOf<SearchElement>()

    /**
     * Draws the searchbar to the screen.
     *
     * @param mouseX The x coordinate of the mouse.
     * @param mouseY The y coordinate of the mouse.
     */
    fun draw(mouseX: Int, mouseY: Int) {
        val currentSearch = Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it.name.lowercase().startsWith(currentInput.lowercase()) }

        searchElements.clear()

        var elementHeight = 0f

        if (currentInput != "") {
            currentSearch.forEach {
                searchElements.add(SearchElement(it, this, y + height + elementHeight))
                elementHeight += fontHeight
            }
        }

        RenderUtil.drawRoundedRect(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble() + elementHeight, 5.0, 5.0, 5.0, 5.0, Color(0, 0, 0, 150).rgb)

        if (!listening) {
            renderText("Search...", x + 3, y + 7, Color(150, 150, 150).rgb)
        } else {
            renderText(currentInput + "_", x + 3, y + 7, -1)
        }

        if (searchElements.isNotEmpty()) {
            searchElements.forEach {
                it.draw(mouseX, mouseY)
            }
        }
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, button: Click) {
        if (mouseX.toFloat() in x..(x + width) && mouseY.toFloat() in y..(y + height)) {
            listening = true
        }

        searchElements.forEach {
            it.mouseClicked(mouseX, mouseY, button)
        }
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, button: Click) {

    }

    fun keyTyped(character: Char, keyCode: Int) {
        if (listening) {
            if (keyCode == Keyboard.KEY_BACK && currentInput.isNotEmpty()) {
                currentInput = currentInput.substring(0, currentInput.length - 1)
                return
            }

            else if (keyCode == Keyboard.KEY_RETURN) {
                currentInput = ""
                listening = false
                return
            }

            if (ChatAllowedCharacters.isAllowedCharacter(character)) {
                currentInput += character
            }
        }
    }

}