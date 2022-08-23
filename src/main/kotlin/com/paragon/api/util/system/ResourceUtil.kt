package com.paragon.api.util.system

import com.paragon.api.util.mc
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.ITextureObject
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.util.ResourceLocation
import org.apache.commons.io.FilenameUtils
import javax.imageio.ImageIO

/**
 * @author SooStrator1136
 */
object ResourceUtil {

    val client = HttpClient(CIO)

    suspend inline fun getFromURL(url: String, exceptionHandler: (Throwable) -> Unit): ResourceLocation {
        val resourceLoc = ResourceLocation("downloaded/${FilenameUtils.getBaseName(url)}")
        val textureObj: ITextureObject? = mc.textureManager.getTexture(resourceLoc)

        if (textureObj == null || textureObj == TextureUtil.MISSING_TEXTURE) {
            runCatching {
                mc.textureManager.loadTexture(
                    resourceLoc,
                    DynamicTexture(ImageIO.read(client.get(url).readBytes().inputStream()))
                )
            }.onFailure {
                exceptionHandler(it)
            }
        }

        return resourceLoc
    }

}