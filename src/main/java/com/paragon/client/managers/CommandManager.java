package com.paragon.client.managers;

import com.paragon.Paragon;
import com.paragon.api.util.Wrapper;
import com.paragon.client.systems.command.Command;
import com.paragon.client.systems.command.impl.*;
import com.paragon.client.systems.module.impl.misc.Cryptic;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author Wolfsurge
 */
public class CommandManager implements Wrapper {

    private String prefix = "$";
    private static String lastCommand = "";

    private final List<Command> commands;

    private final List<String> commonPrefixes = Arrays.asList("/", ".", "*", ";", ",");

    public CommandManager() {
        MinecraftForge.EVENT_BUS.register(this);

        commands = Arrays.asList(
                new ConfigCommand(),
                new HelpCommand(),
                OpenFolderCommand.INSTANCE,
                new SocialCommand(),
                new SyntaxCommand()
        );

        Paragon.INSTANCE.getLogger().info("Loaded Command Manager");
    }

    public void handleCommands(String message, boolean fromConsole) {
        if (message.split(" ").length > 0) {
            boolean commandFound = false;
            String commandName = message.split(" ")[0];

            for (Command command : commands) {
                if (command.getName().equalsIgnoreCase(commandName)) {
                    command.whenCalled(Arrays.copyOfRange(message.split(" "), 1, message.split(" ").length), fromConsole);
                    lastCommand = getPrefix() + message;
                    commandFound = true;
                    break;
                }
            }

            if (!commandFound) {
                sendClientMessage(TextFormatting.RED + "Command not found!", fromConsole);
            }
        }
    }

    public void sendClientMessage(String message, boolean fromConsole) {
        // Only send chat message if the message wasn't sent from the console
        if (!fromConsole) {
            mc.player.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "Paragon " + TextFormatting.WHITE + "> " + message));
        }

        Paragon.INSTANCE.getConsole().addLine(TextFormatting.LIGHT_PURPLE + "Paragon " + TextFormatting.WHITE + "> " + message);
    }

    @SubscribeEvent
    public void onChatMessage(ClientChatEvent event) {
        // Check if the message starts with the prefix
        if (event.getMessage().startsWith(getPrefix())) {
            event.setCanceled(true);

            handleCommands(event.getMessage().substring(prefix.length()), false);
        }
    }

    public boolean startsWithPrefix(String message) {
        return message.split(" ")[0].toLowerCase().startsWith(getPrefix().toLowerCase(Locale.ROOT)) || commonPrefixes.contains(message.split(" ")[0].toLowerCase()) || message.startsWith("crypt") && Cryptic.INSTANCE.isEnabled();
    }

    public String getPrefix() {
        return prefix;
    }

    public List<String> getCommonPrefixes() {
        return commonPrefixes;
    }

    /**
     * Gets the commands
     *
     * @return The commands
     */
    public List<Command> getCommands() {
        return commands;
    }

    /**
     * Gets the last command sent
     *
     * @return The last command sent
     */
    public String getLastCommand() {
        return lastCommand;
    }

}
