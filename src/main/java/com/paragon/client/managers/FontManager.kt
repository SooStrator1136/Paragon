package com.paragon.client.managers

import com.paragon.Paragon
import com.paragon.api.util.render.font.FontRenderer
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.commons.io.Charsets
import org.apache.commons.io.FileUtils
import org.json.JSONObject
import java.awt.Font
import java.io.*
import java.net.URL

/**
 * @author Wolfsurge, SooStrator1136
 */
@SideOnly(Side.CLIENT)
class FontManager {

    val fontRenderer = FontRenderer(getFont())
    var yIncrease = 0f

    private fun getFont(): Font {
        val fontDir = File("paragon/font/")

        if (!fontDir.exists()) {
            fontDir.mkdirs()
        }

        // We need to download the default font
        if ((fontDir.listFiles()?.size ?: 0) < 2) {
            Paragon.INSTANCE.logger.info("Downloading default font...")

            runCatching {
                val fontStream = BufferedInputStream(URL("https://www.dropbox.com/s/wki4fxll36znfu4/font.ttf?dl=1").openStream())
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
            val jsonObject = JSONObject(FileUtils.readFileToString(File("paragon/font/font_config.json"), Charsets.UTF_8))
            size = jsonObject.getInt("size").toFloat()
            yIncrease = jsonObject.getFloat("y_offset")
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var result = Font("default", Font.PLAIN, size.toInt())

        runCatching {
            val fontStream = FileInputStream("paragon/font/font.ttf")
            val font = Font.createFont(0, fontStream)
            fontStream.close()
            result = font.deriveFont(Font.PLAIN, size)
        }

        return result
    }

}