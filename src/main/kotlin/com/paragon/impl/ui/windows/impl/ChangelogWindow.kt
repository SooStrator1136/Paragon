package com.paragon.impl.ui.windows.impl

import com.paragon.util.render.BlurUtil
import com.paragon.util.render.font.FontUtil
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.module.client.Colours
import com.paragon.impl.ui.util.Click
import com.paragon.impl.ui.windows.Window
import com.paragon.util.render.RenderUtil
import net.minecraft.util.math.MathHelper
import org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets
import kotlin.math.max

/**
 * @author Surge
 * @since 27/07/2022
 */
class ChangelogWindow(x: Float, y: Float, width: Float, height: Float, grabbableHeight: Float) : Window(x, y, width, height, grabbableHeight) {

    private val changelog: ArrayList<String> = arrayListOf()
    private var scroll = 0f

    init {
        val inputStream = javaClass.getResourceAsStream("/assets/paragon/changelog.txt")
        changelog.addAll(IOUtils.toString(inputStream, StandardCharsets.UTF_8).split(System.lineSeparator()))
    }

    override fun scroll(mouseX: Int, mouseY: Int, mouseDelta: Int): Boolean {
        if (mouseDelta != 0 && mouseX.toFloat() in x..x + width && mouseY.toFloat() in y..y + height) {
            scroll += FontUtil.getHeight() * if (mouseDelta > 0) 1 else -1

            return true
        }

        return false
    }

    override fun draw(mouseX: Int, mouseY: Int, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        val changelogHeight = changelog.size * FontUtil.getHeight();

        scroll = MathHelper.clamp(scroll.toDouble(), -max(0.0, (changelogHeight - height + grabbableHeight + 6).toDouble()), 0.0).toFloat()

        RenderUtil.drawRect(x, y, (width * openAnimation.getAnimationFactor()).toFloat(), (height * openAnimation.getAnimationFactor()).toFloat(), 0x90000000.toInt())

        if (ClickGUI.blur.value) {
            BlurUtil.blur(x.toInt(), y.toInt(), (width * openAnimation.getAnimationFactor()).toInt(), (height * openAnimation.getAnimationFactor()).toInt(), ClickGUI.intensity.value.toInt())
        }

        RenderUtil.pushScissor(x.toDouble(), y.toDouble(), width * openAnimation.getAnimationFactor(), 16 * openAnimation.getAnimationFactor())

        RenderUtil.drawRect(x, y, width * openAnimation.getAnimationFactor().toFloat(), grabbableHeight, Colours.mainColour.value.rgb)
        FontUtil.drawStringWithShadow("Changelog", x + 3, y + 4, -1)

        RenderUtil.drawRect((x + ((width - 16f) * openAnimation.getAnimationFactor())).toFloat(), y, 16f, grabbableHeight, 0x90000000.toInt())
        FontUtil.font.drawStringWithShadow("X", (x + width - 9f) - (FontUtil.font.getStringWidth("X") / 2f), y + 1.5f, -1)

        RenderUtil.popScissor()

        RenderUtil.drawBorder(x + 0.5f, y + 0.5f, ((width - 1) * openAnimation.getAnimationFactor()).toFloat(), ((height - 1) * openAnimation.getAnimationFactor()).toFloat(), 0.5f, Colours.mainColour.value.rgb)

        RenderUtil.pushScissor(x.toDouble(), y.toDouble() + 17, width.toDouble() * openAnimation.getAnimationFactor(), (height.toDouble() - 18) * openAnimation.getAnimationFactor())

        var offset = grabbableHeight + 5f

        changelog.forEach {
            FontUtil.drawStringWithShadow(it, x + 5, y + offset + scroll, -1)

            offset += FontUtil.getHeight()
        }

        RenderUtil.popScissor()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, click: Click): Boolean {
        if (mouseX.toFloat() in x + width - FontUtil.getStringWidth("X") - 5..x + width && mouseY.toFloat() in y..y + grabbableHeight) {
            openAnimation.state = false
            return true
        }

        // dragging
        val superVal = super.mouseClicked(mouseX, mouseY, click)

        if (mouseX.toFloat() in x..x + width && mouseY.toFloat() in y..y + grabbableHeight + height) {
            return true
        }

        return superVal
    }

}