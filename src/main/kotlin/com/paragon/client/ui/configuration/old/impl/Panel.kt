package com.paragon.client.ui.configuration.old.impl

import com.paragon.Paragon
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.util.render.RenderUtil.drawRect
import com.paragon.api.util.render.RenderUtil.drawRoundedRect
import com.paragon.api.util.render.RenderUtil.popScissor
import com.paragon.api.util.render.RenderUtil.pushScissor
import com.paragon.api.util.render.font.FontUtil.renderCenteredString
import com.paragon.client.systems.module.impl.client.ClickGUI.animationSpeed
import com.paragon.client.systems.module.impl.client.ClickGUI.cornerRadius
import com.paragon.client.systems.module.impl.client.ClickGUI.easing
import com.paragon.client.systems.module.impl.client.ClickGUI.panelHeaderSeparator
import com.paragon.client.systems.module.impl.client.ClientFont
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.old.OldPanelGUI.Companion.isInside
import com.paragon.client.ui.configuration.old.impl.module.ModuleButton
import me.surge.animation.Animation
import java.awt.Color
import java.util.function.Consumer

/**
 * @author Wolfsurge
 */
class Panel(var x: Float, var y: Float, val width: Float, private val barHeight: Float, val category: Category) {

    // List of module buttons
    private val moduleButtons = ArrayList<ModuleButton>()

    // Opening / Closing animation
    private val animation: Animation

    // Variables
    private var dragging = false
    private var lastX = 0f
    private var lastY = 0f

    init {
        var offset = y + barHeight

        // Add a new module button for each module in the category
        for (module in Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { module: Module -> module.category == this.category }) {
            moduleButtons.add(ModuleButton(this, module, offset, 13f))

            // Increase offset
            offset += 13f
        }

        animation = Animation(animationSpeed::value, true, easing::value)
    }

    fun renderPanel(mouseX: Int, mouseY: Int) {
        // Set X and Y
        if (dragging) {
            x = mouseX - lastX
            y = mouseY - lastY
        }

        var height = 0f

        for (moduleButton in moduleButtons) {
            height += moduleButton.absoluteHeight
        }

        // Header
        drawRoundedRect(x.toDouble(), y.toDouble(), width.toDouble(), barHeight.toDouble(), cornerRadius.value.toDouble(), cornerRadius.value.toDouble(), 1.0, 1.0, if (isMouseOverHeader(mouseX, mouseY)) Color(28, 28, 28).rgb else Color(23, 23, 23).darker().rgb)
        renderCenteredString(this.category.Name, x + width / 2f, y + barHeight / 2f + if (ClientFont.isEnabled) 2f else 0.5f, -1, true)
        refreshOffsets()

        pushScissor((x - 0.5f).toDouble(), y.toDouble(), (width + 1).toDouble(), barHeight + height * animation.getAnimationFactor() + 0.5f)

        if (isExpanded) {
            // Draw modules
            moduleButtons.forEach(Consumer { moduleButton: ModuleButton -> moduleButton.renderModuleButton(mouseX, mouseY) })
        }

        popScissor()

        drawRect(x, (y + barHeight + height * animation.getAnimationFactor()).toFloat(), width, 2f, Color(23, 23, 23).darker().rgb)

        if (panelHeaderSeparator.value) {
            drawRect(x, y + barHeight - 1, width, 1f, Colours.mainColour.value.rgb)
        }
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        // Drag the frame if we have clicked on the header
        if (isMouseOverHeader(mouseX, mouseY) && mouseButton == 0) {
            lastX = mouseX - x
            lastY = mouseY - y
            dragging = true
        }

        // Toggle the open state if we right-click on the header
        if (isMouseOverHeader(mouseX, mouseY) && mouseButton == 1) {
            animation.state = !isExpanded
        }

        // Call the mouseClicked event for each module button if the panel is open
        if (isExpanded) {
            moduleButtons.forEach(Consumer { moduleButton: ModuleButton -> moduleButton.mouseClicked(mouseX, mouseY, mouseButton) })
        }
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0) {
            // Make sure we aren't dragging
            dragging = false
        }

        // Mouse released.
        moduleButtons.forEach(Consumer { moduleButton: ModuleButton -> moduleButton.mouseReleased(mouseX, mouseY, mouseButton) })
    }

    fun keyTyped(keyTyped: Char, keyCode: Int) {
        if (isExpanded) {
            moduleButtons.forEach(Consumer { moduleButton: ModuleButton -> moduleButton.keyTyped(keyTyped, keyCode) })
        }
    }

    fun isMouseOverHeader(mouseX: Int, mouseY: Int): Boolean {
        return isInside(x, y, x + width, y + barHeight, mouseX, mouseY)
    }

    /**
     * Sets all the module offsets
     */
    fun refreshOffsets() {
        var y = y + barHeight
        for (moduleButton in moduleButtons) {
            moduleButton.offset = y
            y += (moduleButton.absoluteHeight * animation.getAnimationFactor()).toFloat()
        }
    }

    val isExpanded: Boolean
        get() = animation.getAnimationFactor() > 0
}