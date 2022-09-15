package com.paragon.impl.command.impl

import com.paragon.Paragon
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 */
object SocialCommand : com.paragon.impl.command.Command("Social", "social [add/remove/list] [name] [add - relationship]") {

    override fun whenCalled(args: Array<String>, fromConsole: Boolean) {
        if (args.size == 1 && args[0].equals("list", ignoreCase = true)) {
            // List all players
            if (Paragon.INSTANCE.friendManager.names.isEmpty()) {
                Paragon.INSTANCE.commandManager.sendClientMessage(TextFormatting.RED.toString() + "You haven't added anyone to your social list!", fromConsole)

                return
            }

            for (player in Paragon.INSTANCE.friendManager.names) {
                Paragon.INSTANCE.commandManager.sendClientMessage(
                    player, fromConsole
                )
            }
        }
        else if (args.size == 2 && args[0].equals("add", ignoreCase = true)) {
            // Add a player
            runCatching {
                val name = args[1]

                Paragon.INSTANCE.friendManager.addName(name)

                Paragon.INSTANCE.commandManager.sendClientMessage(
                    TextFormatting.GREEN.toString() + "Added player " + name + " to your socials list!", fromConsole
                )

                // Save social
                Paragon.INSTANCE.storageManager.saveSocial()
            }.onFailure {
                Paragon.INSTANCE.commandManager.sendClientMessage(
                    TextFormatting.RED.toString() + "Invalid argument! Should be 'friend', 'neutral', or 'enemy'", fromConsole
                )
            }
        }
        else if (args.size == 2 && args[0].equals("remove", ignoreCase = true)) {
            // Remove a player
            val name = args[1]
            Paragon.INSTANCE.friendManager.removePlayer(name)
            Paragon.INSTANCE.commandManager.sendClientMessage(
                TextFormatting.GREEN.toString() + "Removed player " + name + " from your socials list!", fromConsole
            )

            // Save socials
            Paragon.INSTANCE.storageManager.saveSocial()
        }
        else {
            // Say that we have given an invalid syntax
            Paragon.INSTANCE.commandManager.sendClientMessage(
                TextFormatting.RED.toString() + "Invalid Syntax!", fromConsole
            )
        }
    }

}