package com.paragon.api.util.render.font

import com.paragon.Paragon
import com.paragon.api.util.Wrapper
import com.paragon.client.systems.module.impl.client.ClientFont
import org.apache.commons.io.FileUtils
import org.json.JSONObject
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
    val defaultFont = FontRenderer(getFont("font"))

    private var yIncrease = 0F

    @JvmStatic
    fun drawStringWithShadow(text: String, x: Float, y: Float, colour: Int) {
        if (ClientFont.isEnabled) {
            defaultFont.drawStringWithShadow(text, x, y - 3f + yIncrease, colour)
            return
        }

        if (text.contains(System.lineSeparator())) {
            val parts = text.split(System.lineSeparator().toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            var newY = 0.0f

            for (s in parts) {
                minecraft.fontRenderer.drawStringWithShadow(s, x, y + newY, colour)
                newY += minecraft.fontRenderer.FONT_HEIGHT.toFloat()
            }

            return
        }

        minecraft.fontRenderer.drawStringWithShadow(text, x, y, colour)
    }

    @JvmStatic
    fun renderCenteredString(text: String, x: Float, y: Float, colour: Int, centeredY: Boolean) {
        var y = y

        if (ClientFont.isEnabled) {
            if (centeredY) {
                y -= defaultFont.height / 2f
            }

            if (text.contains("\n")) {
                val parts = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                var newY = 0.0f

                for (s in parts) {
                    defaultFont.drawStringWithShadow(
                        s,
                        x - defaultFont.getStringWidth(s) / 2f,
                        y - 3.5f + yIncrease + newY,
                        colour
                    )

                    newY += defaultFont.height
                }

                return
            }

            defaultFont.drawStringWithShadow(text, x - getStringWidth(text) / 2f, y - 3f + yIncrease, colour)
            return
        }

        if (centeredY) {
            y -= minecraft.fontRenderer.FONT_HEIGHT / 2f
        }

        minecraft.fontRenderer.drawStringWithShadow(
            text,
            x - minecraft.fontRenderer.getStringWidth(text) / 2f,
            y,
            colour
        )
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
            defaultFont.getStringWidth(text).toFloat()
        } else {
            minecraft.fontRenderer.getStringWidth(text).toFloat()
        }
    }

    @JvmStatic
    fun getHeight() = if (ClientFont.isEnabled) defaultFont.height else minecraft.fontRenderer.FONT_HEIGHT.toFloat()

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
            val font = Font.createFont(0, fontStream)
            fontStream.close()
            result = font.deriveFont(Font.PLAIN, if (name == "ms_sans_serif") 45f else size)
        }

        return result
    }

}