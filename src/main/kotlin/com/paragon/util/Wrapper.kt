package com.paragon.util

import net.minecraft.client.Minecraft

interface Wrapper {

    val minecraft: Minecraft
        get() = Minecraft.getMinecraft()

    fun isHovered(x: Float, y: Float, width: Float, height: Float, mouseX: Int, mouseY: Int): Boolean = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height

}