package com.paragon.client.ui.configuration.windows.impl

import com.paragon.Paragon
import com.paragon.api.util.render.BlurUtil
import com.paragon.api.util.render.RenderUtil
import com.paragon.client.systems.module.impl.client.ClickGUI
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.windows.Window
import com.paragon.client.ui.util.Click
import org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets

/**
 * @author Surge
 * @since 27/07/2022
 */
class ChangelogWindow(x: Float, y: Float, width: Float, height: Float, grabbableHeight: Float) : Window(x, y, width, height, grabbableHeight) {

    val changelog: ArrayList<String> = arrayListOf()

    init {
        val inputStream = javaClass.getResourceAsStream("/assets/paragon/changelog.txt")
        changelog.addAll(IOUtils.toString(inputStream, StandardCharsets.UTF_8).split(System.lineSeparator()))
    }

    override fun draw(mouseX: Int, mouseY: Int, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        RenderUtil.pushScissor(x.toDouble(), y.toDouble(), width.toDouble() * openAnimation.getAnimationFactor(), (height.toDouble() + 1) * openAnimation.getAnimationFactor())

        RenderUtil.drawRect(x, y, width, height, 0x90000000.toInt())

        if (ClickGUI.blur.value) {
            BlurUtil.blur(x.toInt(), y.toInt(), (width * openAnimation.getAnimationFactor()).toInt(), (height * openAnimation.getAnimationFactor()).toInt(), ClickGUI.intensity.value.toInt())
        }

        RenderUtil.drawRect(x, y, width * openAnimation.getAnimationFactor().toFloat(), grabbableHeight, Colours.mainColour.value.rgb)
        renderText("Changelog", x + 3, y + 4, -1)

        RenderUtil.drawBorder(x + 0.5f, y + 0.5f, ((width - 1) * openAnimation.getAnimationFactor()).toFloat(), ((height - 1) * openAnimation.getAnimationFactor()).toFloat(), 0.5f, Colours.mainColour.value.rgb)

        RenderUtil.drawRect(x + width - 16f, y, 16f, grabbableHeight, 0x90000000.toInt())
        Paragon.INSTANCE.fontManager.fontRenderer.drawStringWithShadow("X", (x + width - 9f) - (Paragon.INSTANCE.fontManager.fontRenderer.getStringWidth("X") / 2f), y + 1.5f, -1)

        var offset = grabbableHeight + 5f

        changelog.forEach {
            renderText(it, x + 5, y + offset, -1)

            offset += fontHeight
        }

        RenderUtil.popScissor()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, click: Click): Boolean {
        if (mouseX.toFloat() in x + width - getStringWidth("X") - 5..x + width && mouseY.toFloat() in y..y + grabbableHeight) {
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