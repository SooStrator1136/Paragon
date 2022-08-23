package com.paragon.client.shader.shaders

import com.paragon.client.shader.Shader
import org.lwjgl.opengl.GL20.*
import java.awt.Color

class LiquidShader : Shader("/assets/paragon/glsl/shaders/liquid.frag") {

    private var colour = Color(0, 0, 0)

    fun setColour(colour: Color) {
        this.colour = colour
    }

    override fun setupUniforms() {
        setupUniform("texture")
        setupUniform("resolution")
        setupUniform("time")
        setupUniform("colour")
    }

    override fun updateUniforms() {
        glUniform1i(getUniform("texture"), 0)
        glUniform2f(getUniform("resolution"), 1f / minecraft.displayWidth, 1f / minecraft.displayHeight)
        glUniform1f(getUniform("time"), time.toFloat())
        glUniform4f(getUniform("colour"), colour.red / 255f, colour.green / 255f, colour.blue / 255f, colour.alpha / 255f)
    }

}