package com.paragon.impl.command.impl

import com.paragon.Paragon
import com.paragon.util.system.TextureUtil
import net.minecraft.util.ResourceLocation

/**
 * @author SooStrator1136
 */
object CopySkinCommand : com.paragon.impl.command.Command("CopySkin", "copyskin <player>") {

    var skin: ResourceLocation? = null

    override fun whenCalled(args: Array<String>, fromConsole: Boolean) {
        var shouldSet = true
        val newSkin = TextureUtil.getFromURL("https://minotar.net/skin/${args[0]}.png") {
            Paragon.INSTANCE.commandManager.sendClientMessage("Couldn't load skin!", fromConsole)
            shouldSet = false //Crossinlined lambda, can't return
        }

        if (shouldSet) {
            skin = newSkin
        }
    }

}