package com.paragon.impl.ui.configuration.paragon.panel

import com.paragon.Paragon
import com.paragon.util.render.font.FontUtil
import com.paragon.impl.module.Category
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.ui.configuration.paragon.module.ModuleElement
import com.paragon.impl.ui.configuration.shared.Panel
import com.paragon.impl.ui.util.Click
import com.paragon.util.render.ColourUtil.fade
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.RenderUtil.renderItemStack
import com.paragon.util.string.StringUtil
import com.paragon.util.toBinary
import me.surge.animation.Animation
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.max

/**
 * @author Surge
 * @since 06/08/2022
 */
class CategoryPanel(val category: Category, x: Float, y: Float, width: Float, height: Float) : Panel(x, y, width, height) {

    var tooltipName: String = ""
    var tooltipContent: String = ""

    private val modules = ArrayList<ModuleElement>()

    var scroll = 0f
    var scrollFactor = 0f

    private val expand = Animation(ClickGUI.animationSpeed::value, true, ClickGUI.easing::value)

    var interactableHeight = 0f

    init {
        for (module in Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it.category == category }) {
            modules.add(ModuleElement(module, this, x, y + height, width, 16f))
        }
    }

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        RenderUtil.drawRect(
            x, y, width, height, Color(40, 40, 60).fade(Color(60, 60, 80), isHovered(mouseX, mouseY).toBinary().toDouble()).rgb
        )

        var titleOffset = 5f

        if (ClickGUI.icon.value != ClickGUI.Icon.NONE) {
            if (ClickGUI.icon.value == ClickGUI.Icon.BACKGROUND) {
                RenderUtil.drawRect(x, y, height, height, 0x70000000)
            }

            // Eye of ender is offset weirdly...
            if (this.category === Category.RENDER) {
                GL11.glTranslated(-0.5, -0.5, 0.0)
            }

            renderItemStack(ItemStack(this.category.indicator), x + 2, y + 2, false)

            if (this.category === Category.RENDER) {
                GL11.glTranslated(0.5, 0.5, 0.0)
            }

            titleOffset = 25f
        }

        FontUtil.drawStringWithShadow(StringUtil.getFormattedText(category), x + titleOffset, y + 7, -1)

        RenderUtil.drawRect(x, y + height, width, 320 * expand.getAnimationFactor().toFloat(), Color(32, 32, 46).rgb)

        var moduleHeight = 0f

        modules.forEach {
            moduleHeight += it.getAbsoluteHeight()
        }

        interactableHeight = (MathHelper.clamp(moduleHeight.toDouble(), 0.0, 320.0) * expand.getAnimationFactor()).toFloat()

        RenderUtil.pushScissor(x.toDouble(), y + height.toDouble(), width.toDouble(), (MathHelper.clamp(moduleHeight.toDouble(), moduleHeight.toDouble(), 320.0) * expand.getAnimationFactor()))

        if (mouseDelta != 0 && mouseX in x..x + width && mouseY in y + height..y + height + 320f) {
            scrollFactor = if (mouseDelta > 0) (240f / Minecraft.getDebugFPS()) else -((240f / Minecraft.getDebugFPS()))
            scrollFactor *= 3
        }
        else {
            if (scrollFactor != 0f) {
                scrollFactor *= 0.9f

                if (scrollFactor < 0.1 && scrollFactor > -0.1) {
                    scrollFactor = 0f
                }
            }
        }

        scroll += scrollFactor
        scroll = MathHelper.clamp(scroll.toDouble(), -max(0.0, (moduleHeight - 320.0)), 0.0).toFloat()

        var offset = y + height + scroll

        modules.forEach {
            it.x = x
            it.y = offset

            it.draw(mouseX, mouseY, mouseDelta)

            offset += it.getAbsoluteHeight()
        }

        RenderUtil.popScissor()
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        modules.forEach {
            it.mouseClicked(mouseX, mouseY, click)
        }

        if (isHovered(mouseX, mouseY) && click == Click.RIGHT) {
            expand.state = !expand.state
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseReleased(mouseX, mouseY, click)

        modules.forEach {
            it.mouseReleased(mouseX, mouseY, click)
        }
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        super.keyTyped(character, keyCode)

        modules.forEach {
            it.keyTyped(character, keyCode)
        }
    }

}