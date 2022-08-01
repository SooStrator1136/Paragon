package com.paragon.client.ui.configuration.discord.module

import com.paragon.api.module.Module

import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.font.FontUtil
import com.paragon.client.ui.configuration.discord.GuiDiscord
import com.paragon.client.ui.configuration.discord.IRenderable
import com.paragon.client.ui.configuration.discord.category.CategoryBar
import com.paragon.client.ui.configuration.discord.settings.SettingsBar
import com.paragon.client.ui.util.animation.Animation
import com.paragon.client.ui.util.animation.Easing
import org.lwjgl.opengl.GL11.*
import org.lwjgl.util.Rectangle
import java.awt.Color
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

/**
 * @author SooStrator1136
 */
object ModuleBar : IRenderable {

    var focusedModule: Module? = null

    private var actualHeight = 0
    var scrollOffset = 0

    val shownModules: MutableList<DiscordModule> = ArrayList()
    val rect = Rectangle()
    private val userRect = Rectangle()

    private val nameAnimation = Animation({ 2000F }, false, { Easing.LINEAR })
    var lastCopyTime = 0L

    override fun render(mouseX: Int, mouseY: Int) {
        //Set the basic bounds
        run {
            rect.setBounds(
                CategoryBar.rect.x + CategoryBar.rect.width,
                CategoryBar.rect.y,
                CategoryBar.rect.width * 3,
                CategoryBar.rect.height - 30
            )
            userRect.setBounds(
                rect.x,
                rect.y + rect.height,
                rect.width,
                30
            )
        }

        //Render the actual modules
        run {
            //Scroll logic
            if (GuiDiscord.D_WHEEL != 0 && shownModules.isNotEmpty() && rect.contains(mouseX, mouseY)) {
                val newScrollOffset = (scrollOffset + GuiDiscord.D_WHEEL / 7.5).toInt()
                if (GuiDiscord.D_WHEEL < 0) {
                    scrollOffset = newScrollOffset
                } else if (scrollOffset < 0) {
                    scrollOffset = if (newScrollOffset > 0) 0 else newScrollOffset
                }
            }

            var yOffset = 0
            shownModules.forEachIndexed { i, module ->
                module.rect.setBounds(rect.x + 2, rect.y + 2 + yOffset + scrollOffset, rect.width - 4, 20)
                yOffset = (20 * (i + 1)) + (5 * (i + 1))
            }
            actualHeight = yOffset - 25

            RenderUtil.drawRect(
                rect.x.toFloat(),
                rect.y.toFloat(),
                rect.width.toFloat(),
                rect.height.toFloat(),
                GuiDiscord.CHANNEL_BAR_BACKGROUND.rgb
            )

            RenderUtil.pushScissor(
                rect.x.toDouble(),
                rect.y + 1.0,
                rect.width.toDouble(),
                rect.height.toDouble(),
            )

            shownModules.forEach {
                it.render(mouseX, mouseY)
            }

            RenderUtil.popScissor()
        }

        //Render the user info
        run {
            RenderUtil.drawRect(
                userRect.x.toFloat(),
                userRect.y.toFloat(),
                userRect.width.toFloat(),
                userRect.height.toFloat(),
                GuiDiscord.USER_FIELD_BACKGROUND.rgb
            )
            renderHead(userRect.x + 3, userRect.y + 2, 20)

            if (FontUtil.getStringWidth(mc.player.name) > userRect.width - 30.0) {
                nameAnimation.state = userRect.contains(mouseX, mouseY)
            }

            RenderUtil.pushScissor(
                userRect.x + 30.0,
                userRect.y.toDouble(),
                userRect.width - 30.0,
                userRect.height.toDouble()
            )
            FontUtil.drawStringWithShadow(
                mc.player.name,
                (((userRect.x + 30F) - ((FontUtil.getStringWidth(mc.player.name) - (userRect.width - 30F)) * nameAnimation.getAnimationFactor())).toFloat()),
                (userRect.y + (userRect.height / 2F)) - (FontUtil.getHeight() / 2),
                Color.WHITE.rgb
            )
            RenderUtil.popScissor()

            //Render the "copied" thing after the name was copied
            if (lastCopyTime != 0L) {
                RenderUtil.drawRoundedRect(
                    (userRect.x + ((userRect.width - FontUtil.getStringWidth("Copied!")) / 2.0)) - 2.0,
                    (userRect.y - (FontUtil.getHeight() / 2.0)) - 1.5,
                    FontUtil.getStringWidth("Copied!") + 4.0,
                    FontUtil.getHeight() + 3.0,
                    5.0,
                    5.0,
                    5.0,
                    5.0,
                    GuiDiscord.USER_COPIED_COLOR.rgb
                )

                FontUtil.renderCenteredString(
                    "Copied!",
                    (userRect.x + (userRect.width / 2)).toFloat(),
                    userRect.y.toFloat(),
                    Color.WHITE.rgb,
                    true
                )

                if (System.currentTimeMillis() - 1500L > lastCopyTime) {
                    lastCopyTime = 0L
                }
            }
        }
    }

    private fun renderHead(x: Int, y: Int, size: Int) {
        glPushMatrix()
        glTranslatef(x.toFloat(), y.toFloat(), 0F)
        val scaleFac = size / 32.0
        glScaled(scaleFac, scaleFac, 1.0)
        glTranslatef(-x.toFloat(), -y.toFloat(), 0F)
        mc.textureManager.bindTexture(mc.player.locationSkin)
        glEnable(GL_BLEND)
        (mc.currentScreen ?: return).drawTexturedModalRect(x + 5, y + 5, 32, 32, 32, 32)
        glDisable(GL_BLEND)
        glPopMatrix()
    }

    override fun onClick(mouseX: Int, mouseY: Int, button: Int) {
        if (userRect.contains(mouseX, mouseY)) {
            lastCopyTime = System.currentTimeMillis()
            Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(mc.player.name), null)
        } else if (rect.contains(mouseX, mouseY)) {
            for (module in shownModules) {
                if (module.rect.contains(mouseX, mouseY)) {
                    focusedModule = module.module
                    SettingsBar.shownSettings.clear()
                    SettingsBar.scrollOffset = 0
                    break
                }
            }
        }
    }

    override fun onKey(keyCode: Int) {}

}