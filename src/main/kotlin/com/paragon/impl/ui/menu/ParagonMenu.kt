package com.paragon.impl.ui.menu

import com.paragon.Paragon
import com.paragon.impl.module.client.Colours
import com.paragon.impl.module.client.MainMenu
import com.paragon.util.render.BlurUtil
import com.paragon.util.render.font.FontUtil
import com.paragon.util.render.RenderUtil
import me.surge.animation.Animation
import me.surge.animation.ColourAnimation
import me.surge.animation.Easing
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.shader.Framebuffer
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
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

    private val optionsButton = Button(FontUtil.Icon.GEAR, "Options", (width / 2f) - 66, height - 70f) {
        mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
    }

    private val exitButton = Button(FontUtil.Icon.CLOSE, "Exit", (width / 2f) + 2, height - 70f) {
        mc.shutdown()
    }

    private val buttons = arrayOf(
        singleplayerButton,
        multiplayerButton,
        optionsButton,
        exitButton
    )

    private val githubHover = ColourAnimation(Color.WHITE, Colours.mainColour.value, { 200f }, false, { Easing.LINEAR })

    private val whiteFade = ColourAnimation(Color.WHITE, Color(0, 0, 0, 0), { 1200f }, false, { Easing.LINEAR })
    private val buttonSlide = Animation({ 400f }, false, { Easing.CIRC_IN_OUT })

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        mc.textureManager.bindTexture(ResourceLocation("paragon", "textures/background.png"))
        RenderUtil.drawModalRectWithCustomSizedTexture(0f, 0f, 0f, 0f, width.toFloat(), height.toFloat(), width.toFloat(), height.toFloat())
        BlurUtil.blur(0f, 0f, width.toFloat(), height.toFloat(), 20f)

        RenderUtil.scaleTo((width / 2f) - FontUtil.fontLarge.getStringWidth("Paragon"), 35f, 0f, buttonSlide.getAnimationFactor() * 2, buttonSlide.getAnimationFactor() * 2, 0.0) {
            FontUtil.fontLarge.drawString(
                "Paragon",
                (width / 2f) - FontUtil.fontLarge.getStringWidth("Paragon"),
                35f,
                Color.WHITE,
                false
            )
        }

        FontUtil.font.drawString(Paragon.modVersion, (width / 2f) + 85, 35f, Color.WHITE, false)

        githubHover.state = mouseX.toFloat() in 0f..34f && mouseY.toFloat() in height - FontUtil.icons.height - 20..height.toFloat()

        RenderUtil.scaleTo((-25 + (40 * buttonSlide.getAnimationFactor())).toFloat() + 5f, height - FontUtil.icons.height - 15, 0f, 1.5, 1.5, 0.0) {
            FontUtil.drawIcon(FontUtil.Icon.GITHUB, (-25 + (40 * buttonSlide.getAnimationFactor())).toFloat() - 5f, height - FontUtil.icons.height - 15, githubHover.getColour())
        }

        var x = (width / 2f) - ((buttons.size * 68f) / 2f)
        var yIncrease = 0

        buttons.forEach {
            it.x = x
            it.y = (height - (120 + (yIncrease - (yIncrease * buttonSlide.getAnimationFactor()) * buttonSlide.getAnimationFactor())) * buttonSlide.getAnimationFactor()).toFloat()

            it.render(mouseX, mouseY)

            x += 68
            yIncrease += 20
        }

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
        buttons.forEach { it.mouseClicked() }

        if (githubHover.state) {
            Desktop.getDesktop().browse(URI("https://github.com/Wolfsurge/Paragon"))
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    internal class Button(val icon: FontUtil.Icon, val name: String, var x: Float, var y: Float, val onClick: Runnable) {
        val hover = Animation({ 200f }, false, { Easing.CUBIC_IN_OUT })

        fun render(mouseX: Int, mouseY: Int) {
            hover.state = mouseX.toFloat() in x..x + 64 && mouseY.toFloat() in y..y + 64

            RenderUtil.scaleTo(x + 32, y + 32, 0f, 1 - (0.25 * hover.getAnimationFactor()), 1 - (0.25 * hover.getAnimationFactor()), 1.0) {
                RenderUtil.drawRoundedRect(
                    x,
                    y - (16 * hover.getAnimationFactor()).toFloat(),
                    64f,
                    64f,
                    31f,
                    Color(0, 0, 0, 150)
                )

                RenderUtil.scaleTo(
                    x + 11f + if (icon == FontUtil.Icon.PERSON) 7f else 0f + if (icon == FontUtil.Icon.GEAR) 4f else 0f + if (icon == FontUtil.Icon.CLOSE) 7f else 0f,
                    y + 10 - (16 * hover.getAnimationFactor()).toFloat() + if (icon == FontUtil.Icon.GEAR) 2f else 0f + if (icon == FontUtil.Icon.CLOSE) 2f else 0f,
                    0f,
                    2.0,
                    2.0,
                    0.0
                ) {
                    FontUtil.drawIcon(
                        icon,
                        x + 11f + if (icon == FontUtil.Icon.PERSON) 7f else 0f + if (icon == FontUtil.Icon.GEAR) 4f else 0f + if (icon == FontUtil.Icon.CLOSE) 7f else 0f,
                        y + 10 - (16 * hover.getAnimationFactor()).toFloat() + if (icon == FontUtil.Icon.GEAR) 2f else 0f + if (icon == FontUtil.Icon.CLOSE) 2f else 0f,
                        Color.WHITE
                    )
                }
            }

            FontUtil.font.drawString(name, (x + 32) - (FontUtil.font.getStringWidth(name) / 2), y + 64 - (16 * hover.getAnimationFactor()).toFloat(), Color(255, 255, 255, (255 * hover.getAnimationFactor()).toInt().coerceAtLeast(5)), false)
        }

        fun mouseClicked() {
            if (hover.state) {
                onClick.run()
            }
        }

    }

}