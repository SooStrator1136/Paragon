package com.paragon.client.systems.command.impl

import com.paragon.Paragon
import com.paragon.api.util.system.ResourceUtil
import com.paragon.client.systems.command.Command
import net.minecraft.util.ResourceLocation

/**
 * @author SooStrator1136
 */
object CopySkinCommand : Command("CopySkin", "copyskin") {

    var skin: ResourceLocation? = null

    override fun whenCalled(args: Array<String>, fromConsole: Boolean) {
        var shouldSet = true
        val newSkin = ResourceUtil.getFromURL("https://minotar.net/skin/${args[0]}.png") {
            Paragon.INSTANCE.commandManager.sendClientMessage("Couldn't load skin!", fromConsole)
            shouldSet = false //Crossinlined lambda, can't return
        }

        if (shouldSet) {
            skin = newSkin
        }
    }

}