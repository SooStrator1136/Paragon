package com.paragon.client.systems.module.hud.impl

import com.paragon.Paragon
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.RenderUtil.drawModalRectWithCustomSizedTexture
import com.paragon.api.util.render.RenderUtil.scaleTo
import com.paragon.api.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.client.systems.module.hud.HUDModule
import com.paragon.client.systems.module.impl.client.Colours
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.TextFormatting

object Watermark : HUDModule("Watermark", "Renders the client's name on screen") {

    private val icon = ResourceLocation("paragon", "textures/paragon.png")

    private val display = Setting("Display", Display.TEXT, null, null, null) describedBy "The type of watermark to display"
    private val scaleFac = Setting("Size", 1.0, 0.1, 2.0, 0.05) describedBy "The scale of the image watermark" visibleWhen  { display.value == Display.IMAGE }

    override fun render() {
        when (display.value) {
            Display.TEXT -> drawStringWithShadow("Paragon " + TextFormatting.GRAY + Paragon.modVersion, x, y, Colours.mainColour.value.rgb)

            Display.IMAGE -> {
                minecraft.textureManager.bindTexture(icon)
                val width = 880 / 4.0f
                val height = 331 / 4.0f

                scaleTo(x, y, 0.0f, scaleFac.value, scaleFac.value, 1.0) {
                    drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, width, height, width, height)
                }
            }

            else -> {}
        }
    }

    override var width: Float
        get() = display.value!!.width

        set(width) {
            super.width = width
        }

    override var height: Float
        get() = display.value!!.height

        set(height) {
            super.height = height
        }

    enum class Display(val width: Float, val height: Float) {
        /**
         * Watermark will be text
         */
        TEXT(Minecraft.getMinecraft().fontRenderer.getStringWidth("Paragon " + Paragon.modVersion).toFloat(), 12f),

        /**
         * Watermark will be an image
         */
        IMAGE(160f, 25f);

    }

}