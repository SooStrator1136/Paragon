package com.paragon.client.systems.module.hud

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.util.render.RenderUtil.screenHeight
import com.paragon.api.util.render.RenderUtil.screenWidth
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.math.MathHelper
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

/**
 * @author Surge, SooStrator1136
 */
@SideOnly(Side.CLIENT)
abstract class HUDModule(name: String, description: String) : Module(name, Category.HUD, description) {

    open var width = 50F
    open var height = 50F

    var x = 50F
    var y = 50F
    private var lastX = 0F
    private var lastY = 0F
    var isDragging = false
        private set

    init {
        setVisible(false)
    }

    abstract fun render()

    fun updateComponent(mouseX: Int, mouseY: Int) {
        // Set X and Y
        if (isDragging) {
            val sr = ScaledResolution(minecraft)
            val newX = MathHelper.clamp(mouseX - lastX, 1f, screenWidth)
            val newY = MathHelper.clamp(mouseY - lastY, 1f, screenHeight)

            println(sr.scaleFactor)

            x = newX
            y = newY

            val centerX = newX + width / 2f
            val centerY = newY + height / 2f

            if (centerX > sr.scaledWidth / 2f - 5 && centerX < sr.scaledWidth / 2f + 5) {
                x = sr.scaledWidth / 2f - width / 2f
            }

            if (centerY > sr.scaledHeight / 2f - 5 && centerY < sr.scaledHeight / 2f + 5) {
                y = sr.scaledHeight / 2f - height / 2f
            }
        }
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (isHovered(x, y, width, height, mouseX, mouseY)) {
            if (mouseButton == 0) {
                lastX = mouseX - x
                lastY = mouseY - y
                isDragging = true
            } else if (mouseButton == 1) {
                if (isEnabled) {
                    toggle()
                    return true
                }
            }
        }
        return false
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        isDragging = false
    }

}