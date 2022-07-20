package com.paragon.client.ui.configuration.window.search

import com.paragon.api.module.Module
import com.paragon.api.util.render.ITextRenderer
import com.paragon.client.ui.configuration.window.element.module.ModuleElement
import com.paragon.client.ui.configuration.window.window.windows.ConfigurationWindow
import com.paragon.client.ui.util.Click
import java.util.stream.Collectors

/**
 * @author Wolfsurge
 */
class SearchElement(val module: Module, private val searchBar: SearchBar, val y: Float) : ITextRenderer {

    fun draw(mouseX: Int, mouseY: Int) {
        renderText(module.name, searchBar.x + 5, y, -1)
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, button: Click) {
        if (mouseX.toFloat() in searchBar.x..searchBar.x + searchBar.width && mouseY.toFloat() in y..y + searchBar.height) {
            (searchBar.window as ConfigurationWindow).selectedCategory.expandAnimation.state = false
            searchBar.window.selectedCategory = searchBar.window.getCategoryElement(module.category)
            searchBar.window.selectedCategory.expandAnimation.state = true

            val moduleElement = searchBar.window.selectedCategory.subElements.stream().filter { it is ModuleElement && it.module == module }.collect(Collectors.toList())[0] as ModuleElement

            moduleElement.expandAnimation.state = true
            moduleElement.flash.state = true

            if (moduleElement.y < searchBar.window.y + 30) {
                searchBar.window.scrollFactor = moduleElement.y - searchBar.window.y
            }

            if (moduleElement.y > searchBar.window.y + searchBar.window.height) {
                searchBar.window.scrollFactor = moduleElement.y - searchBar.window.y - searchBar.window.height
            }

            searchBar.listening = false
            searchBar.currentInput = ""
            searchBar.searchElements = arrayListOf()
        }
    }

}