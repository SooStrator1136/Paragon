package com.paragon.impl.ui.menu

import com.paragon.Paragon
import com.paragon.impl.module.client.Colours
import com.paragon.impl.module.client.MainMenu
import com.paragon.util.render.ColourUtil.integrateAlpha
import com.paragon.util.render.font.FontUtil
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.shader.Shader
import me.surge.animation.Animation
import me.surge.animation.ColourAnimation
import me.surge.animation.Easing
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL20.*
import java.awt.Color
import java.awt.Desktop
import java.net.URI

/**
 * @author Surge
 */
class ParagonMenu : GuiScreen() {

    private var frameBuffer: Framebuffer? = null
    private var lastScaleFactor = 0f
    private var lastScaleWidth = 0f
    private var lastScaleHeight = 0f

    private val singleplayerButton = Button(FontUtil.Icon.PERSON, "Singleplayer", (width / 2f) - 66, height - 70f) {
        mc.displayGuiScreen(GuiWorldSelection(this))
    }

    private val multiplayerButton = Button(FontUtil.Icon.PEOPLE, "Multiplayer", (width / 2f) + 2, height - 70f) {
        mc.displayGuiScreen(GuiMultiplayer(this))
    }

    private val settingHover = ColourAnimation(Color.WHITE, Colours.mainColour.value, { 200f }, false, { Easing.LINEAR })
    private val exitHover = ColourAnimation(Color.WHITE, Colours.mainColour.value, { 200f }, false, { Easing.LINEAR })
    private val githubHover = ColourAnimation(Color.WHITE, Colours.mainColour.value, { 200f }, false, { Easing.LINEAR })

    private val whiteFade = ColourAnimation(Color.WHITE, Color(0, 0, 0, 0), { 1200f }, false, { Easing.LINEAR })
    private val buttonSlide = Animation({ 400f }, false, { Easing.CIRC_IN_OUT })

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        GlStateManager.pushMatrix()
        GlStateManager.pushAttrib()

        val res = ScaledResolution(mc)

        // Delete old framebuffer
        if (frameBuffer != null) {
            frameBuffer!!.framebufferClear()
            if (lastScaleFactor != res.scaleFactor.toFloat() ||
                lastScaleWidth != width.toFloat() ||
                lastScaleHeight != height.toFloat()) {

                frameBuffer!!.deleteFramebuffer()
                frameBuffer = Framebuffer(mc.displayWidth, mc.displayHeight, true)
                frameBuffer!!.framebufferClear()
            }

            lastScaleFactor = res.scaleFactor.toFloat()
            lastScaleWidth = res.scaledWidth.toFloat()
            lastScaleHeight = res.scaledHeight.toFloat()
        } else {
            frameBuffer = Framebuffer(mc.displayWidth, mc.displayHeight, true)
        }

        frameBuffer!!.bindFramebuffer(false)

        RenderUtil.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Color.WHITE)

        frameBuffer!!.unbindFramebuffer()
        mc.framebuffer.bindFramebuffer(true)
        GlStateManager.pushMatrix()

        MainMenu.drawShader()

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, frameBuffer!!.framebufferTexture)
        GL11.glBegin(GL11.GL_QUADS)
        GL11.glTexCoord2d(0.0, 1.0)
        GL11.glVertex2d(0.0, 0.0)
        GL11.glTexCoord2d(0.0, 0.0)
        GL11.glVertex2d(0.0, res.scaledHeight.toDouble())
        GL11.glTexCoord2d(1.0, 0.0)
        GL11.glVertex2d(res.scaledWidth.toDouble(), res.scaledHeight.toDouble())
        GL11.glTexCoord2d(1.0, 1.0)
        GL11.glVertex2d(res.scaledWidth.toDouble(), 0.0)
        GL11.glEnd()

        glUseProgram(0)
        GL11.glPopMatrix()
        GlStateManager.popMatrix()
        GlStateManager.popAttrib()

        RenderUtil.drawRect(0f, -70 + (100f * buttonSlide.getAnimationFactor().toFloat()), width.toFloat(), 70f, Color(0, 0, 0, 150))

        RenderUtil.scaleTo((width / 2f) - FontUtil.fontLarge.getStringWidth("Paragon"), (-180 + (180 * buttonSlide.getAnimationFactor()).toFloat()) + 35f, 0f, 2.0, 2.0, 0.0) {
            FontUtil.fontLarge.drawString("Paragon", (width / 2f) - FontUtil.fontLarge.getStringWidth("Paragon"), (-180 + (180 * buttonSlide.getAnimationFactor()).toFloat()) + 35f, Color.WHITE, false)
        }

        FontUtil.font.drawString(Paragon.modVersion, width / 2f - FontUtil.font.getStringWidth("Paragon") / 2f, -20 + 103f * buttonSlide.getAnimationFactor().toFloat(), Color.WHITE, false)

        settingHover.state = mouseX.toFloat() in 0f..70f && mouseY.toFloat() in 30f..100f

        RenderUtil.scaleTo(15f, -50 + (95f * buttonSlide.getAnimationFactor().toFloat()), 0f, 2.0, 2.0, 2.0) {
            FontUtil.drawIcon(FontUtil.Icon.GEAR, 15f, -50 + (95f * buttonSlide.getAnimationFactor().toFloat()), settingHover.getColour())
        }

        exitHover.state = mouseX.toFloat() in width - 25 - FontUtil.icons.getStringWidth(FontUtil.Icon.CLOSE.char.toString()).toFloat() * 4..width.toFloat() && mouseY.toFloat() in 30f..100f

        RenderUtil.scaleTo(width - FontUtil.icons.getStringWidth(FontUtil.Icon.CLOSE.char.toString()).toFloat() * 4, -50 + (95f * buttonSlide.getAnimationFactor().toFloat()), 0f, 2.0, 2.0, 2.0) {
            FontUtil.drawIcon(FontUtil.Icon.CLOSE, width - FontUtil.icons.getStringWidth(FontUtil.Icon.CLOSE.char.toString()).toFloat() * 4, -50 + (95f * buttonSlide.getAnimationFactor().toFloat()), exitHover.getColour())
        }

        githubHover.state = mouseX.toFloat() in 0f..34f && mouseY.toFloat() in height - FontUtil.icons.height - 20..height.toFloat()

        RenderUtil.scaleTo((-25 + (40 * buttonSlide.getAnimationFactor())).toFloat() + 5f, height - FontUtil.icons.height - 15, 0f, 1.5, 1.5, 0.0) {
            FontUtil.drawIcon(FontUtil.Icon.GITHUB, (-25 + (40 * buttonSlide.getAnimationFactor())).toFloat() - 5f, height - FontUtil.icons.height - 15, githubHover.getColour())
        }

        singleplayerButton.x = ((width * buttonSlide.getAnimationFactor()).toFloat() / 2f) - 66
        singleplayerButton.y = height - 70f

        singleplayerButton.render(mouseX, mouseY)

        multiplayerButton.x = (width * buttonSlide.getAnimationFactor().toFloat() / 2f + 2) + (width - (width * buttonSlide.getAnimationFactor()).toFloat())
        multiplayerButton.y = height - 70f

        multiplayerButton.render(mouseX, mouseY)

        super.drawScreen(mouseX, mouseY, partialTicks)

        whiteFade.state = true
        buttonSlide.state = whiteFade.getAnimationFactor() == 1.0
        RenderUtil.drawRect(0f, 0f, width.toFloat(), height.toFloat(), whiteFade.getColour())
    }

    override fun onGuiClosed() {
        whiteFade.resetToDefault()
        buttonSlide.resetToDefault()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        singleplayerButton.mouseClicked(mouseX, mouseY)
        multiplayerButton.mouseClicked(mouseX, mouseY)

        if (settingHover.state) {
            mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
        }

        if (exitHover.state) {
            mc.shutdown()
        }

        if (githubHover.state) {
            Desktop.getDesktop().browse(URI("https://github.com/Wolfsurge/Paragon"))
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    internal class Button(val icon: FontUtil.Icon, val name: String, var x: Float, var y: Float, val onClick: Runnable) {
        val hover = Animation({ 400f }, false, { Easing.CUBIC_IN_OUT })

        fun render(mouseX: Int, mouseY: Int) {
            hover.state = mouseX.toFloat() in x..x + 64 && mouseY.toFloat() in y..y + 64

            RenderUtil.drawRoundedRect(x, y - (16 * hover.getAnimationFactor()).toFloat(), 64f, 64f, 3f, Color(0, 0, 0, 150))

            RenderUtil.scaleTo(x + 10 + if (icon == FontUtil.Icon.PERSON) 7f else 0f, y + 10 - (16 * hover.getAnimationFactor()).toFloat(), 0f, 2.0, 2.0, 0.0) {
                FontUtil.drawIcon(icon, x + 11 + if (icon == FontUtil.Icon.PERSON) 7f else 0f, y + 10 - (16 * hover.getAnimationFactor()).toFloat(), Color.WHITE)
            }

            FontUtil.font.drawString(name, (x + 32) - (FontUtil.font.getStringWidth(name) / 2), y + 64 - (16 * hover.getAnimationFactor()).toFloat(), Color(255, 255, 255, (255 * hover.getAnimationFactor()).toInt().coerceAtLeast(5)), false)
        }

        fun mouseClicked(mouseX: Int, mouseY: Int) {
            if (hover.state) {
                onClick.run()
            }
        }

    }

}