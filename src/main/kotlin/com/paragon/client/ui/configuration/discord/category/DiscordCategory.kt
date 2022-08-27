package com.paragon.client.ui.configuration.discord.category

import com.paragon.api.module.Category
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.RenderUtil.scaleTo
import com.paragon.api.util.render.font.FontUtil
import com.paragon.api.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.api.util.render.font.FontUtil.getStringWidth
import com.paragon.client.systems.module.impl.client.ClickGUI
import com.paragon.client.ui.configuration.discord.GuiDiscord
import com.paragon.client.ui.configuration.discord.IRenderable
import me.surge.animation.Animation
import net.minecraft.item.ItemStack
import java.awt.Color
import java.awt.Rectangle

/**
 * @author SooStrator1136
 */
class DiscordCategory(val category: Category) : IRenderable {

    val rect = Rectangle()

    private val indicator = ItemStack(category.indicator)
    private val nameAnimation = Animation({ 200F }, false, ClickGUI.easing::value)

    override fun render(mouseX: Int, mouseY: Int) {
        val diff = rect.width / 10
        rect.setBounds(rect.x + diff, rect.y + diff, rect.width - (diff * 2), rect.height - (diff * 2))

        val isHovered = rect.contains(mouseX, mouseY)

        //Render the basic icon with its background
        run {
            @Suppress("IncorrectFormatting")
            RenderUtil.drawRoundedRect(
                rect.x.toDouble(),
                rect.y.toDouble(),
                rect.width.toDouble(),
                rect.height.toDouble(),
                15.0,
                15.0,
                15.0,
                15.0,
                if (isHovered) GuiDiscord.CHANNEL_BAR_BACKGROUND.brighter().rgb else GuiDiscord.CHANNEL_BAR_BACKGROUND.rgb
            )

            val scaleFac = rect.width / 16.0
            scaleTo(rect.x.toFloat(), rect.y.toFloat(), 0F, scaleFac, scaleFac, 1.0) {
                RenderUtil.renderItemStack(indicator, rect.x.toFloat(), rect.y.toFloat(), false)
            }
        }

        //Render the name tooltip
        run {
            nameAnimation.state = isHovered

            RenderUtil.pushScissor(
                rect.x + rect.width - 2.0,
                rect.y.toDouble(),
                (getStringWidth(category.Name) + 6) * nameAnimation.getAnimationFactor(),
                rect.height.toDouble()
            )

            RenderUtil.drawRoundedRect(
                (rect.x + rect.width).toDouble() - 2.0,
                (rect.centerY - (FontUtil.getHeight() / 2)),
                getStringWidth(category.Name).toDouble() + 6,
                FontUtil.getHeight().toDouble(),
                5.0,
                5.0,
                5.0,
                5.0,
                GuiDiscord.CATEGORY_TEXT_BACKGROUND.rgb
            )
            drawStringWithShadow(
                category.Name,
                (rect.x + rect.width).toFloat(),
                (rect.centerY - 4).toFloat(),
                Color.WHITE.rgb
            )

            RenderUtil.popScissor()
        }
    }

    override fun onClick(mouseX: Int, mouseY: Int, button: Int) {}
    override fun onRelease(mouseX: Int, mouseY: Int, button: Int) {}

    override fun onKey(keyCode: Int) {}

}