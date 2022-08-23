package com.paragon.client.ui.menu

import com.paragon.Paragon
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.font.FontUtil
import com.paragon.client.systems.module.impl.client.Colours
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.client.gui.*
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.glScalef
import java.io.IOException

/**
 * @author Surge
 */
class ParagonMenu : GuiScreen() {

    // Credits expand animation
    private val creditsAnimation = Animation({ 500.0f }, false) { Easing.EXPO_IN_OUT }

    // Whether the credits are displayed or not
    private var creditsExpanded = false

    override fun initGui() {
        buttonList.add(ParagonButton(0, width / 2 - 100, height / 2, 200, 20, "Singleplayer"))
        buttonList.add(ParagonButton(1, width / 2 - 100, height / 2 + 25, 200, 20, "Multiplayer"))
        buttonList.add(ParagonButton(2, width / 2 - 100, height / 2 + 50, 95, 20, "Options"))
        buttonList.add(ParagonButton(3, width / 2 + 5, height / 2 + 50, 95, 20, "Exit"))
        buttonList.add(ParagonButton(4, 3, height - 23, 60, 20, "Credits"))
        buttonList.add(ParagonButton(5, width - 83, 3, 80, 20, "Minecraft Menu"))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        // Get background offsets
        val xOffset = -1.0f * ((mouseX - width / 10.0f) / (width / 10.0f))
        val yOffset = -1.0f * ((mouseY - height / 10.0f) / (height / 10.0f))

        // Draw background
        mc.textureManager.bindTexture(ResourceLocation("paragon", "textures/background.png"))
        RenderUtil.drawModalRectWithCustomSizedTexture(
            xOffset,
            yOffset,
            0f,
            0f,
            width + 10f,
            height + 10f,
            width + 10f,
            height + 10f
        )

        // Draw button background
        RenderUtil.drawRoundedRect(
            (width / 2f - 110).toDouble(),
            (height / 2f - 50).toDouble(),
            220.0,
            130.0,
            5.0,
            5.0,
            5.0,
            5.0,
            -0x80000000
        )

        // Title
        glScalef(2.5f, 2.5f, 2.5f)
        run {
            val scaleFactor = 1.0f / 2.5f
            FontUtil.renderCenteredString(
                "Paragon",
                width / 2f * scaleFactor,
                (height / 2f - 30) * scaleFactor,
                Colours.mainColour.value.rgb,
                true
            )
            glScalef(scaleFactor, scaleFactor, scaleFactor)
        }

        // Version
        glScalef(0.8f, 0.8f, 0.8f)
        run {
            val scaleFactor = 1 / 0.8f
            FontUtil.renderCenteredString(
                "v" + Paragon.modVersion,
                width / 2f * scaleFactor,
                (height / 2f - 10) * scaleFactor,
                0xFFFFFF,
                true
            )
            glScalef(scaleFactor, scaleFactor, scaleFactor)
        }

        // Credits
        RenderUtil.pushScissor(5.0, 250.0, 200 * creditsAnimation.getAnimationFactor(), 300.0)

        // Rect
        RenderUtil.drawRoundedRect(
            5.0,
            250.0,
            200 * creditsAnimation.getAnimationFactor(),
            60.0,
            5.0,
            5.0,
            5.0,
            5.0,
            -0x80000000
        )

        // Title
        FontUtil.drawStringWithShadow("Credits", 10f, 255f, -1)

        // Credits
        glScalef(0.65f, 0.65f, 0.65f)
        run {
            val scaleFactor = 1 / 0.65f
            var y = 270f
            for (str in arrayOf(
                "Created by Surge & Teletofu",
                "Shader OpenGL code - linustouchtips",
                "Font Renderer - Cosmos Client"
            )) {
                FontUtil.drawStringWithShadow(str, 10 * scaleFactor, y * scaleFactor, -1)
                y += 10f
            }
            glScalef(scaleFactor, scaleFactor, scaleFactor)
        }

        // End scissor
        RenderUtil.popScissor()
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(IOException::class)
    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(GuiWorldSelection(this))
            1 -> mc.displayGuiScreen(GuiMultiplayer(this))
            2 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            3 -> mc.shutdown()
            4 -> {
                creditsExpanded = !creditsExpanded
                creditsAnimation.state = creditsExpanded
            }

            5 -> {
                Paragon.INSTANCE.isParagonMainMenu = false
                mc.displayGuiScreen(GuiMainMenu())
            }
        }
        super.actionPerformed(button)
    }

}