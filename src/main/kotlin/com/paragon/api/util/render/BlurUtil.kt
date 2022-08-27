package com.paragon.api.util.render

import com.paragon.api.util.Wrapper
import com.paragon.mixins.accessor.IShaderGroup
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.shader.Framebuffer
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11.*

/**
 * @author Surge
 * @since 27/07/22
 */
@SideOnly(Side.CLIENT)
object BlurUtil : Wrapper {

    private var lastScale = -1
    private var lastScaleWidth = -1
    private var lastScaleHeight = -1
    private var framebuffer: Framebuffer? = null
    private var blurShader: ShaderGroup? = null

    private fun checkScale(scaleFactor: Int, widthFactor: Int, heightFactor: Int) {
        if (lastScale != scaleFactor || lastScaleWidth != widthFactor || lastScaleHeight != heightFactor || framebuffer == null || blurShader == null) {
            try {
                blurShader = ShaderGroup(minecraft.textureManager, minecraft.resourceManager, minecraft.framebuffer, ResourceLocation("shaders/post/blur.json"))
                blurShader!!.createBindFramebuffers(minecraft.displayWidth, minecraft.displayHeight)
                framebuffer = (blurShader as IShaderGroup?)!!.mainFramebuffer
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

        lastScale = scaleFactor
        lastScaleWidth = widthFactor
        lastScaleHeight = heightFactor
    }

    private fun updateUniforms(intensity: Int) {
        (blurShader as IShaderGroup?)!!.listShaders[0]?.shaderManager?.getShaderUniform("Radius")!!.set(intensity.toFloat())
        (blurShader as IShaderGroup?)!!.listShaders[1]?.shaderManager?.getShaderUniform("Radius")!!.set(intensity.toFloat())
        (blurShader as IShaderGroup?)!!.listShaders[0]?.shaderManager?.getShaderUniform("BlurDir")!![2f] = 1f
        (blurShader as IShaderGroup?)!!.listShaders[1]?.shaderManager?.getShaderUniform("BlurDir")!![1f] = 1f
    }

    fun blur(x: Int, y: Int, width: Int, height: Int, intensity: Int) {
        val resolution = ScaledResolution(minecraft)
        val currentScale = resolution.scaleFactor
        checkScale(currentScale, resolution.scaledWidth, resolution.scaledHeight)

        if (OpenGlHelper.isFramebufferEnabled()) {
            RenderUtil.pushScissor(x.toDouble(), (y + 1).toDouble(), width.toDouble(), (height - 1).toDouble())
            updateUniforms(intensity)

            framebuffer!!.bindFramebuffer(true)
            blurShader!!.render(minecraft.renderPartialTicks)

            minecraft.framebuffer.bindFramebuffer(true)

            RenderUtil.popScissor()

            GlStateManager.enableBlend()
            GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ZERO, GL_ONE)

            framebuffer!!.framebufferRenderExt(minecraft.displayWidth, minecraft.displayHeight, false)
            GlStateManager.disableBlend()
            glScalef(currentScale.toFloat(), currentScale.toFloat(), 0f)
        }
    }
}