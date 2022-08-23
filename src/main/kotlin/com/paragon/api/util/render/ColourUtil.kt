package com.paragon.api.util.render

import org.lwjgl.opengl.GL11.glColor4f
import java.awt.Color

object ColourUtil {

    /**
     * Creates a rainbow wave
     *
     * @param time How long for each wave
     * @param saturation The saturation of the colour
     * @param addition How much hue to add to the wave
     * @return A rainbow in the RGB format
     */
    fun getRainbow(time: Float, saturation: Float, addition: Int): Int {
        val timeFac = if (time.isNaN()) 0.0 else time * 1000.0
        val hue = (System.currentTimeMillis() + addition) % (time * 1000).toInt() / if (timeFac == 0.0) 1.0 else timeFac
        return Color.HSBtoRGB(hue.toFloat(), saturation, 1f)
    }

    /**
     * Sets the GL colour based on a hex integer
     *
     * @param colourHex The integer of the hex value
     */
    @JvmStatic
    fun setColour(colourHex: Int) {
        val alpha = (colourHex shr 24 and 0xFF) / 255.0f
        val red = (colourHex shr 16 and 0xFF) / 255.0f
        val green = (colourHex shr 8 and 0xFF) / 255.0f
        val blue = (colourHex and 0xFF) / 255.0f
        glColor4f(red, green, blue, alpha)
    }

    /**
     * Integrates alpha into a colour
     *
     * @param alpha  The new alpha
     * @return The new colour
     */
    @JvmStatic
    fun Color.integrateAlpha(alpha: Float): Color {
        val red = this.red / 255f
        val green = this.green / 255f
        val blue = this.blue / 255f
        return Color(red, green, blue, alpha / 255f)
    }

    /**
     * Stolen from Monsoon 3.0 lel
     * (im dev so its fine (probably))
     */
    @JvmStatic
    fun Color.fade(secondary: Color, factor: Double): Color {
        val redDiff: Int = secondary.red - this.red
        val greenDiff: Int = secondary.green - this.green
        val blueDiff: Int = secondary.blue - this.blue
        val alphaDiff: Int = secondary.alpha - this.alpha

        val newRed = (this.red + redDiff * factor).toInt()
        val newGreen = (this.green + greenDiff * factor).toInt()
        val newBlue = (this.blue + blueDiff * factor).toInt()
        val newAlpha = (this.alpha + alphaDiff * factor).toInt()

        return Color(newRed, newGreen, newBlue, newAlpha)
    }
}