package com.paragon.client.ui.configuration.discord

import net.minecraft.client.Minecraft

/**
 * @author SooStrator1136
 */
interface IRenderable {

    val mc: Minecraft
        get() = Minecraft.getMinecraft()

    fun render(mouseX: Int, mouseY: Int)
    fun onClick(mouseX: Int, mouseY: Int, button: Int)
    fun onRelease(mouseX: Int, mouseY: Int, button: Int)
    fun onKey(keyCode: Int)

}