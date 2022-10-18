package com.paragon.impl.ui.windows.impl

import com.paragon.util.render.BlurUtil
import com.paragon.util.render.font.FontUtil
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.module.client.Colours
import com.paragon.impl.ui.util.Click
import com.paragon.impl.ui.windows.Window
import com.paragon.util.render.RenderUtil
import com.paragon.util.toColour
import net.minecraft.util.math.MathHelper
import org.apache.commons.io.IOUtils
import java.awt.Color
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

        scroll = MathHelper.clamp(
            scroll.toDouble(),
            -max(0.0, (changelogHeight - height + grabbableHeight + 6).toDouble()),
            0.0
        ).toFloat()

        RenderUtil.drawRect(
            x,
            y + grabbableHeight,
            (width * openAnimation.getAnimationFactor()).toFloat(),
            ((height - grabbableHeight) * openAnimation.getAnimationFactor()).toFloat(),
            Color(0, 0, 0, 120)
        )

        if (ClickGUI.blur.value) {
            BlurUtil.blur(
                x,
                y + grabbableHeight,
                (width * openAnimation.getAnimationFactor()).toFloat(),
                ((height - grabbableHeight) * openAnimation.getAnimationFactor()).toFloat(),
                ClickGUI.intensity.value
            )
        }

        RenderUtil.drawRect(
            x,
            y,
            width * openAnimation.getAnimationFactor().toFloat(),
            grabbableHeight,
            Colours.mainColour.value
        )

        RenderUtil.pushScissor(
            x,
            y,
            width * openAnimation.getAnimationFactor().toFloat(),
            16 * openAnimation.getAnimationFactor().toFloat()
        )

        FontUtil.drawStringWithShadow("Changelog", x + 3, y + 4, Color.WHITE)

        RenderUtil.scaleTo((x + width - 7f) - FontUtil.font.getStringWidth("X"), y + 1, 0f, 0.7, 0.7, 0.7) {
            FontUtil.drawIcon(FontUtil.Icon.CLOSE, (x + width - 7f) - FontUtil.font.getStringWidth("X"), y + 1, Color.WHITE)
        }

        RenderUtil.popScissor()

        RenderUtil.drawBorder(x, y, (width * openAnimation.getAnimationFactor()).toFloat(), (height * openAnimation.getAnimationFactor()).toFloat(), 0.5f, Colours.mainColour.value)

        RenderUtil.pushScissor(x, y + 17, width * openAnimation.getAnimationFactor().toFloat(), (height - 18) * openAnimation.getAnimationFactor().toFloat())

        var offset = grabbableHeight + 5f

        changelog.forEach {
            FontUtil.drawStringWithShadow(it, x + 5, y + offset + scroll, Color.WHITE)

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