package com.paragon.impl.command.impl

import com.paragon.Paragon

/**
 * @author Surge
 */
object HelpCommand : com.paragon.impl.command.Command("Help", "help") {

    override fun whenCalled(args: Array<String>, fromConsole: Boolean) {
        Paragon.INSTANCE.commandManager.commands.forEach {
            Paragon.INSTANCE.commandManager.sendClientMessage(it.name, fromConsole)
        }
    }

}