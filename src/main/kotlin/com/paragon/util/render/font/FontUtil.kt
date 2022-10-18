package com.paragon.util.render.font

import com.paragon.Paragon
import com.paragon.impl.module.client.ClientFont
import com.paragon.util.Wrapper
import org.apache.commons.io.FileUtils
import org.json.JSONObject
import java.awt.Color
import java.awt.Font
import java.io.*
import java.net.URL
import java.nio.charset.StandardCharsets
import kotlin.math.max

/**
 * @author Surge
 * @since 31/07/2022
 */
object FontUtil : Wrapper {

    @JvmStatic
    lateinit var font: FontRenderer

    @JvmStatic
    lateinit var fontLarge: FontRenderer

    @JvmStatic
    lateinit var icons: FontRenderer

    private var yIncrease = 0F

    fun init() {
        font = FontRenderer(getFont("font"))
        fontLarge = FontRenderer(getFont(FileInputStream("paragon/font/font.ttf"), 80f))
        icons = FontRenderer(Font.createFont(0, javaClass.getResourceAsStream("/assets/paragon/font/icons.ttf")).deriveFont(Font.PLAIN, 80f))
    }

    @JvmStatic
    fun drawStringWithShadow(text: String, x: Float, y: Float, colour: Color) {
        if (ClientFont.isEnabled) {
            font.drawStringWithShadow(text, x, y - 3f + yIncrease, colour)
            return
        }

        if (text.contains(System.lineSeparator())) {
            val parts = text.split(System.lineSeparator().toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            var newY = 0.0f

            for (s in parts) {
                minecraft.fontRenderer.drawStringWithShadow(s, x, y + newY, colour.rgb)
                newY += minecraft.fontRenderer.FONT_HEIGHT.toFloat()
            }

            return
        }

        minecraft.fontRenderer.drawStringWithShadow(text, x, y, colour.rgb)
    }

    @JvmStatic
    fun drawCenteredString(text: String, x: Float, y: Float, colour: Color, centeredY: Boolean) {
        /* var yOffset = y

        if (ClientFont.isEnabled) {
            if (centeredY) {
                yOffset = y - (font.height / 2f)
            }

            if (text.contains("\n")) {
                val parts = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                var newY = 0.0f

                for (s in parts) {
                    font.drawStringWithShadow(s, x - font.getStringWidth(s) / 2f, yOffset + yIncrease + newY, colour)

                    newY += font.height
                }

                return
            }

            font.drawStringWithShadow(text, x - getStringWidth(text) / 2f, yOffset + yIncrease, colour)
            return
        }

        if (centeredY) {
            yOffset = y - (minecraft.fontRenderer.FONT_HEIGHT / 2f)
        }

        minecraft.fontRenderer.drawStringWithShadow(text, x - minecraft.fontRenderer.getStringWidth(text) / 2f, yOffset, colour.rgb) */

        if (ClientFont.isEnabled) {
            font.drawStringWithShadow(text, x - (font.getStringWidth(text) / 2f), y - 3f + yIncrease, colour)
        } else {
            if (text.contains(System.lineSeparator())) {
                val parts = text.split(System.lineSeparator().toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                var newY = 0.0f

                for (s in parts) {
                    minecraft.fontRenderer.drawStringWithShadow(s, x - minecraft.fontRenderer.getStringWidth(s) / 2f, y + newY, colour.rgb)
                    newY += minecraft.fontRenderer.FONT_HEIGHT.toFloat()
                }

                return
            }

            minecraft.fontRenderer.drawStringWithShadow(text, x - minecraft.fontRenderer.getStringWidth(text) / 2f, y, colour.rgb)
        }
    }

    @JvmStatic
    fun drawIcon(icon: Icon, x: Float, y: Float, colour: Color) {
        icons.drawString(icon.char.toString(), x, y, colour, false)
    }

    @JvmStatic
    fun getStringWidth(text: String): Float {
        if (text.contains("\n")) {
            val parts = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var width = 0f

            for (s in parts) {
                width = max(width, getStringWidth(s))
            }

            return width
        }

        return if (ClientFont.isEnabled) {
            font.getStringWidth(text).toFloat()
        }
        else {
            minecraft.fontRenderer.getStringWidth(text).toFloat()
        }
    }

    @JvmStatic
    fun getHeight() = if (ClientFont.isEnabled) font.height + 1 else minecraft.fontRenderer.FONT_HEIGHT.toFloat()

    private fun getFont(name: String): Font {
        val fontDir = File("paragon/font/")

        if (!fontDir.exists()) {
            fontDir.mkdirs()
        }

        // We need to download the default font
        if ((fontDir.listFiles()?.size ?: 0) < 2) {
            Paragon.INSTANCE.logger.info("Downloading default font...")

            runCatching {
                val fontStream = BufferedInputStream(
                    URL("https://github.com/Wolfsurge/Paragon/raw/master/resources/font.ttf").openStream()
                )

                val fileOutputStream = FileOutputStream("paragon/font/font.ttf")
                val dataBuffer = ByteArray(1024)
                var bytesRead: Int

                while (fontStream.read(dataBuffer, 0, 1024).also { bytesRead = it } != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead)
                }

                Paragon.INSTANCE.logger.info("Downloaded default font")
            }.onFailure {
                Paragon.INSTANCE.logger.error("Failed to download default font")
            }

            try {
                val json = JSONObject()
                val fileWriter = FileWriter(File("paragon/font/font_config.json"))

                try {
                    json.put("size", 40)
                    json.put("y_offset", 0)
                    fileWriter.write(json.toString(4))
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                fileWriter.flush()
                fileWriter.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        var size = 40f

        try {
            val jsonObject = JSONObject(
                FileUtils.readFileToString(File("paragon/font/font_config.json"), StandardCharsets.UTF_8)
            )

            size = jsonObject.getInt("size").toFloat()
            yIncrease = jsonObject.getFloat("y_offset")
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var result = Font("default", Font.PLAIN, size.toInt())

        runCatching {
            val fontStream = FileInputStream("paragon/font/$name.ttf")
            return getFont(fontStream, size)
        }

        return result
    }

    private fun getFont(stream: InputStream, size: Float): Font {
        val font = Font.createFont(0, stream)
        stream.close()
        return font.deriveFont(Font.PLAIN, size)
    }

    enum class Icon(val char: Char) {
        PEOPLE('a'),
        PERSON('b'),
        ARROW_LEFT('c'),
        CODE_ARROWS('d'),
        CONSOLE('e'),
        WINDOWS('f'),
        MOUSE_POINT('g'),
        MOUSE_GRAB('h'),
        MOUSE_IDLE('i'),
        ERROR('j'),
        WARNING('k'),
        COPY('l'),
        SLIDERS('m'),
        EYE('n'),
        HIDDEN_EYE('o'),
        GEARS('p'),
        GEAR('q'),
        BIN('r'),
        BIN_FILLED('s'),
        DOWNLOAD('t'),
        UPLOAD('u'),
        TICK('v'),
        GITHUB('w'),
        TERMINAL('x'),
        CUBOID('y'),
        POWER('z'),
        EYE_ALTERNATE('A'),
        EYE_FILLED('B'),
        EYE_HIDDEN_FILLED('C'),
        RUNNING('D'),
        EXIT('E'),
        CLOSE('F'),
        BLOCK('G')
    }

}