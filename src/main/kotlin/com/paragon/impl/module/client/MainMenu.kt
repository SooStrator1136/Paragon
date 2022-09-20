package com.paragon.impl.module.client

import com.paragon.Paragon
import com.paragon.bus.listener.Listener
import com.paragon.impl.event.render.gui.GuiUpdateEvent
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.impl.ui.menu.ParagonMenu
import com.paragon.util.render.shader.Shader
import net.minecraft.client.gui.GuiMainMenu
import net.minecraftforge.common.MinecraftForge
import org.lwjgl.opengl.GL20

object MainMenu : Module("MainMenu", Category.CLIENT, "Use the client's custom main menu") {

    private val shader = Setting("Shader", FragmentShader.CHILL) describedBy "The shader to use"

    private val chill = object : Shader("/assets/paragon/glsl/shaders/menu_shader.frag") {

        override fun setupUniforms() {
            setupUniform("resolution")
            setupUniform("time")
        }

        override fun updateUniforms() {
            GL20.glUniform2f(getUniform("resolution"), minecraft.displayWidth.toFloat(), minecraft.displayHeight.toFloat())
            GL20.glUniform1f(getUniform("time"), time.toFloat())
        }

    }

    private val vortex = object : Shader("/assets/paragon/glsl/shaders/menu_shader_new.frag") {

        override fun setupUniforms() {
            setupUniform("colour")
            setupUniform("resolution")
            setupUniform("time")
        }

        override fun updateUniforms() {
            GL20.glUniform3f(getUniform("colour"),
                    Colours.mainColour.value.red / 255f,
                    Colours.mainColour.value.green / 255f,
                    Colours.mainColour.value.blue / 255f
            )

            GL20.glUniform2f(getUniform("resolution"), minecraft.displayWidth.toFloat(), minecraft.displayHeight.toFloat())
            GL20.glUniform1f(getUniform("time"), time.toFloat())
        }

    }

    init {
        // Enabled by default
        isEnabled = true

        // Register events
        MinecraftForge.EVENT_BUS.register(this)
        Paragon.INSTANCE.eventBus.register(this)
    }

    fun drawShader() {
        when (shader.value) {
            FragmentShader.CHILL -> {
                chill.time += 0.01
                chill.startShader()
            }

            FragmentShader.VORTEX -> {
                vortex.time += 0.01
                vortex.startShader()
            }
        }
    }

    @Listener
    fun onGuiUpdate(event: GuiUpdateEvent) {
        if (event.screen is GuiMainMenu) {
            minecraft.displayGuiScreen(ParagonMenu())
            event.cancel()
        }
    }

    enum class FragmentShader {
        CHILL,
        VORTEX,
    }

}