package com.paragon.client.systems.command.impl

import com.paragon.Paragon
import com.paragon.client.managers.social.Player
import com.paragon.client.managers.social.Relationship
import com.paragon.client.systems.command.Command
import net.minecraft.util.text.TextFormatting
import java.util.*

/**
 * @author Surge
 */
object SocialCommand : Command("Social", "social [add/remove/list] [name] [add - relationship]") {

    override fun whenCalled(args: Array<String>, fromConsole: Boolean) {
        if (args.size == 1 && args[0].equals("list", ignoreCase = true)) {
            // List all players
            if (Paragon.INSTANCE.socialManager.players.isEmpty()) {
                Paragon.INSTANCE.commandManager.sendClientMessage(TextFormatting.RED.toString() + "You haven't added anyone to your social list!", fromConsole)
                return
            }
            for (player in Paragon.INSTANCE.socialManager.players) {
                Paragon.INSTANCE.commandManager.sendClientMessage(player.name + " - " + player.relationship.textFormatting + player.relationship, fromConsole)
            }
        } else if (args.size == 3 && args[0].equals("add", ignoreCase = true)) {
            // Add a player
            runCatching {
                val name = args[1]
                val player = Player(name, Relationship.valueOf(args[2].uppercase(Locale.getDefault())))
                Paragon.INSTANCE.socialManager.addPlayer(player)
                Paragon.INSTANCE.commandManager.sendClientMessage(TextFormatting.GREEN.toString() + "Added player " + name + " to your socials list!", fromConsole)

                // Save social
                Paragon.INSTANCE.storageManager.saveSocial()
            }.onFailure {
                Paragon.INSTANCE.commandManager.sendClientMessage(TextFormatting.RED.toString() + "Invalid argument! Should be 'friend', 'neutral', or 'enemy'", fromConsole)
            }
        } else if (args.size == 2 && args[0].equals("remove", ignoreCase = true)) {
            // Remove a player
            val name = args[1]
            Paragon.INSTANCE.socialManager.removePlayer(name)
            Paragon.INSTANCE.commandManager.sendClientMessage(TextFormatting.GREEN.toString() + "Removed player " + name + " from your socials list!", fromConsole)

            // Save socials
            Paragon.INSTANCE.storageManager.saveSocial()
        } else {
            // Say that we have given an invalid syntax
            Paragon.INSTANCE.commandManager.sendClientMessage(TextFormatting.RED.toString() + "Invalid Syntax!", fromConsole)
        }
    }

}