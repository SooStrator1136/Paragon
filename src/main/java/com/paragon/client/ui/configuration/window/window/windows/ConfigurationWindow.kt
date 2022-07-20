package com.paragon.client.ui.configuration.window.window.windows

import com.paragon.Paragon
import com.paragon.api.module.Category
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.string.StringUtil
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.window.element.category.CategoryElement
import com.paragon.client.ui.configuration.window.element.module.ModuleElement
import com.paragon.client.ui.configuration.window.search.SearchBar
import com.paragon.client.ui.configuration.window.window.Window
import com.paragon.client.ui.util.Click
import net.minecraft.util.math.MathHelper
import net.minecraft.util.text.TextFormatting.*
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11.glScalef
import java.awt.Color
import java.util.stream.Collectors

/**
 * @author Wolfsurge
 */
class ConfigurationWindow(val title: String, x: Float, y: Float, width: Float, height: Float) : Window(x, y, width, height) {

    var dragging: Boolean = false
    var lastX: Float = 0f
    var lastY: Float = 0f

    val searchBar = SearchBar(this, x + width - 205, y + 3, 200f, 20f)

    var categoryElements: ArrayList<CategoryElement> = ArrayList()

    var selectedCategory: CategoryElement

    init {
        var offset = 30f
        for (category in Category.values()) {
            categoryElements.add(CategoryElement(category, this, x + 3f, y + offset, 90f, 16f))
            offset += 16f
        }

        selectedCategory = categoryElements[0]
        selectedCategory.expandAnimation.state = true
    }

    override fun draw(mouseX: Int, mouseY: Int) {
        if (dragging) {
            x = mouseX - lastX
            y = mouseY - lastY
        }

        RenderUtil.drawRect(x, y, width, height, Color(50, 50, 55).rgb)

        // ******************************* TITLE BAR ******************************* //

        val factor = 1 / 1.5f

        glScalef(1.5f, 1.5f, 1.5f)

        renderText(title, (x + 4) * factor, (y + 8) * factor, -1)

        glScalef(factor, factor, factor)

        RenderUtil.drawRect(x, y + 25.5f, width, 2f, Colours.mainColour.value.rgb)



        // ******************************* OFFSET ******************************* //

        // This is the offset for each element list
        var offset = 4f



        // ******************************* CATEGORIES ******************************* //

        var categoryOffset = y + 30

        // Module background
        RenderUtil.drawRect((x + 2 + (94 * selectedCategory.expandAnimation.getAnimationFactor())).toFloat(), y + 29, selectedCategory.subElements[0].width + 2f, height - 31f, Color(65, 65, 70).rgb)

        val moduleX = (x + 2 + (95f * selectedCategory.expandAnimation.getAnimationFactor()))
        val moduleY = y + 30
        val moduleWidth = selectedCategory.subElements[0].width

        var moduleHeightVar = 0f
        selectedCategory.subElements.forEach {
            moduleHeightVar += it.getTotalHeight()
        }

        val moduleHeight = MathHelper.clamp(moduleHeightVar, 0f, height - 33f)

        val mouseWheelDelta = Mouse.getDWheel()

        // Module scroll
        if (mouseWheelDelta != 0 && mouseX.toDouble() in moduleX..moduleX + moduleWidth && mouseY.toDouble() in moduleY..moduleY + moduleHeight) {
            scrollFactor = if (mouseWheelDelta > 0) 1f else -1f
        }

        if (scrollFactor != 0f) { //0.0625
            scroll += if (scrollFactor > 0) -1 else 1

            scrollFactor += if (scrollFactor > 0) -0.03125f else 0.03125f

            if (scrollFactor > -0.1f && scrollFactor < 0.1f) {
                scrollFactor = 0f
            }
        }

        scroll = if (moduleHeightVar > height - 33f) {
            MathHelper.clamp(scroll, -moduleHeightVar + moduleHeight + 1, 0f)
        } else {
            0f
        }

        // comedy
        val sorted = categoryElements.stream().filter { true } .collect(Collectors.toList())

        sorted.sortBy { it == selectedCategory }

        // Render module components behind categories
        RenderUtil.startGlScissor(moduleX, moduleY.toDouble(), moduleWidth.toDouble(), moduleHeight.toDouble())

        sorted.forEach { it.renderModuleComponents(mouseX, mouseY, scroll) }

        RenderUtil.endGlScissor()

        // Category background
        RenderUtil.drawRect(x + 2, y + 29, 92f, height - 31f, Color(65, 65, 70).rgb)

        // Then render categories
        categoryElements.forEach {
            it.updatePosition(x + 3f, categoryOffset)
            it.draw(mouseX, mouseY)

            categoryOffset += 16f
        }

        offset += selectedCategory.getTotalWidth() + 6

        // ******************************* INFO ******************************* //
        val infoWidth = width - offset - 1f

        RenderUtil.drawRect(x + offset, y + 29f, infoWidth, height - 31f, Color(65, 65, 70).rgb)

        var infoTitle = ""
        var infoDetails = ""

        selectedCategory.subElements.forEach {
            if (it.isHovered(mouseX, mouseY) && it is ModuleElement) {
                infoTitle = it.module.name
                infoDetails = StringUtil.wrap(it.module.description, (infoWidth / Paragon.INSTANCE.fontManager.fontRenderer.getCharWidth('W')).toInt())
            }
        }

        categoryElements.forEach {
            if (it.isHovered(mouseX, mouseY)) {
                infoTitle = it.category.Name
                infoDetails = StringUtil.wrap("The modules in the ${it.category.name.lowercase()} category", 40)
            }
        }

        glScalef(1.5f, 1.5f, 1.5f)
        renderCenteredString("$UNDERLINE" + infoTitle, (x + offset + (infoWidth / 2f)) * 0.6666667f, (y + 32f) * 0.6666667f, -1, false)
        glScalef(0.6666667f, 0.6666667f, 0.6666667f)

        renderCenteredString(infoDetails, x + offset + (infoWidth / 2f), y + 29f + fontHeight * 2, -1, false)

        // ******************************* SEARCH BAR ******************************* //

        searchBar.x = x + width - 205
        searchBar.y = y + 3

        searchBar.draw(mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Click) {
        if (isHoveredOverBar(mouseX, mouseY)) {
            lastX = mouseX.toFloat() - x
            lastY = mouseY.toFloat() - y

            dragging = true

            return
        }

        categoryElements.forEach {
            if (it.isHovered(mouseX, mouseY)) {
                selectedCategory.expandAnimation.state = false
                selectedCategory = it
                selectedCategory.expandAnimation.state = !selectedCategory.expandAnimation.state

                return@forEach
            }
        }

        if (selectedCategory.expandAnimation.getAnimationFactor() == 1.0) {
            selectedCategory.subElements.forEach {
                it.mouseClicked(mouseX, mouseY, button)
            }
        }

        searchBar.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, button: Click) {
        dragging = false

        if (selectedCategory.expandAnimation.getAnimationFactor() == 1.0) {
            selectedCategory.subElements.forEach {
                it.mouseReleased(mouseX, mouseY, button)
            }
        }

        searchBar.mouseReleased(mouseX, mouseY, button)
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        if (selectedCategory.expandAnimation.getAnimationFactor() == 1.0) {
            selectedCategory.subElements.forEach {
                it.keyTyped(character, keyCode)
            }
        }

        searchBar.keyTyped(character, keyCode)
    }

    private fun isHoveredOverBar(mouseX: Int, mouseY: Int): Boolean = mouseX.toFloat() in x..(x + (width - searchBar.width)) && mouseY.toFloat() in y..(y + 26)

    fun getCategoryElement(categoryIn: Category) = categoryElements.stream().filter { it.category == categoryIn }.findFirst().orElse(null)

}