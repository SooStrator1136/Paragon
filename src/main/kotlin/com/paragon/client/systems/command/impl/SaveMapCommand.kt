package com.paragon.client.systems.command.impl

import com.paragon.Paragon
import com.paragon.client.systems.command.Command
import com.paragon.mixins.accessor.IEntityRenderer
import com.paragon.mixins.accessor.IMapItemRenderer
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.item.ItemMap
import net.minecraftforge.fml.common.ObfuscationReflectionHelper
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * @author SooStrator1136
 */
object SaveMapCommand : Command("SaveMap", "savemap") {

    override fun whenCalled(args: Array<String>, fromConsole: Boolean) {
        val heldStack = if (minecraft.player.heldItemOffhand.item is ItemMap) {
            minecraft.player.heldItemOffhand
        } else minecraft.player.heldItemMainhand
        if (heldStack.item !is ItemMap) {
            Paragon.INSTANCE.commandManager.sendClientMessage("You aren't holding a map!", fromConsole)
            return
        }

        val mapData = (heldStack.item as ItemMap).getMapData(heldStack, minecraft.world) ?: return
        ((minecraft.entityRenderer as IEntityRenderer).mapItemRenderer as IMapItemRenderer).loadedMaps.forEach { (id, instance) ->
            if (id == mapData.mapName) {
                runCatching {
                    val textureData = (ObfuscationReflectionHelper.findField(
                        instance.javaClass,
                        "mapTexture"
                    ).also { it.isAccessible = true }[instance] as DynamicTexture).textureData
                    saveMap(heldStack.displayName, textureData)
                }.onFailure {
                    it.printStackTrace()
                }
                return
            }
        }
    }

    private fun saveMap(name: String, colors: IntArray) {
        File("paragon${File.separator}maps").mkdir()

        var saveLoc = File("paragon${File.separator}maps${File.separator}${name}.jpg")
        var index = 0
        while (saveLoc.exists()) {
            index++
            saveLoc = if (index == 1) {
                File("paragon${File.separator}maps${File.separator}${name} (1).jpg")
            } else {
                File(saveLoc.path.substring(0, saveLoc.path.length - (6 + index.toString().length)) + "(${index}).jpg")
            }
        }

        ImageIO.write(
            BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB).also {
                it.setRGB(
                    0,
                    0,
                    128,
                    128,
                    colors,
                    0,
                    128
                )
            },
            "jpg",
            saveLoc
        )
    }

}