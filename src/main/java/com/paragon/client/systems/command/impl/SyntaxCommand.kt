package com.paragon.client.systems.command.impl

import com.paragon.Paragon
import com.paragon.client.systems.command.Command
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 */
object SyntaxCommand : Command("Syntax", "syntax [command]") {

    override fun whenCalled(args: Array<String>, fromConsole: Boolean) {
        if (args.size == 1) {
            for (command in Paragon.INSTANCE.commandManager.commands) {
                if (command.name.equals(args[0], ignoreCase = true)) {
                    Paragon.INSTANCE.commandManager.sendClientMessage(command.syntax, fromConsole)
                    break
                }
            }
        } else {
            Paragon.INSTANCE.commandManager.sendClientMessage(TextFormatting.RED.toString() + "Invalid syntax!", fromConsole)
        }
    }

}