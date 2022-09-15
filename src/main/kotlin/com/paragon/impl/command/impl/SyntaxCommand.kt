package com.paragon.impl.command.impl

import com.paragon.Paragon
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 */
object SyntaxCommand : com.paragon.impl.command.Command("Syntax", "syntax [command]") {

    override fun whenCalled(args: Array<String>, fromConsole: Boolean) {
        if (args.size == 1) {
            for (command in Paragon.INSTANCE.commandManager.commands) {
                if (command.name.equals(args[0], true)) {
                    Paragon.INSTANCE.commandManager.sendClientMessage(command.syntax, fromConsole)
                    break
                }
            }
        }
        else {
            Paragon.INSTANCE.commandManager.sendClientMessage(
                TextFormatting.RED.toString() + "Invalid syntax!", fromConsole
            )
        }
    }

}