package com.paragon.client.systems.command.impl

import com.paragon.Paragon
import com.paragon.client.systems.command.Command

/**
 * @author Surge
 */
object HelpCommand : Command("Help", "help") {

    override fun whenCalled(args: Array<String>, fromConsole: Boolean) {
        Paragon.INSTANCE.commandManager.commands.forEach {
            Paragon.INSTANCE.commandManager.sendClientMessage(it.name, fromConsole)
        }
    }

}