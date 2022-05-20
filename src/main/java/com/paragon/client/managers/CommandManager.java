package com.paragon.client.managers;

import com.paragon.Paragon;
import com.paragon.api.util.Wrapper;
import com.paragon.client.systems.command.Command;
import com.paragon.client.systems.command.impl.ConfigCommand;
import com.paragon.client.systems.command.impl.HelpCommand;
import com.paragon.client.systems.command.impl.SocialCommand;
import com.paragon.client.systems.command.impl.SyntaxCommand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Wolfsurge
 */
public class CommandManager implements Wrapper {

    public static String prefix = "$";

    private static ArrayList<Command> commands = new ArrayList<>();

    public CommandManager() {
        MinecraftForge.EVENT_BUS.register(this);

        commands.add(new ConfigCommand());
        commands.add(new HelpCommand());
        commands.add(new SocialCommand());
        commands.add(new SyntaxCommand());

        Paragon.INSTANCE.getLogger().info("Loaded Command Manager");
    }

    public static void handleCommands(String message, boolean fromConsole) {
        if (message.split(" ").length > 0) {
            boolean commandFound = false;
            String commandName = message.split(" ")[0];

            for (Command command : commands) {
                if (command.getName().equalsIgnoreCase(commandName)) {
                    command.whenCalled(Arrays.copyOfRange(message.split(" "), 1, message.split(" ").length), fromConsole);
                    commandFound = true;
                    break;
                }
            }

            if (!commandFound) {
                sendClientMessage(TextFormatting.RED + "Command not found!", fromConsole);
            }
        }
    }

    public static void sendClientMessage(String message, boolean fromConsole) {
        // Only send chat message if the message wasn't sent from the console
        if (!fromConsole) {
            mc.player.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "Paragon " + TextFormatting.WHITE + "> " + message));
        }

        Paragon.INSTANCE.getConsole().addLine(TextFormatting.LIGHT_PURPLE + "Paragon " + TextFormatting.WHITE + "> " + message);
    }

    @SubscribeEvent
    public void onChatMessage(ClientChatEvent event) {
        // Check if the message starts with the prefix
        if (event.getMessage().startsWith(prefix)) {
            event.setCanceled(true);

            handleCommands(event.getMessage().substring(prefix.length()), false);
        }
    }

    /**
     * Gets the commands
     *
     * @return The commands
     */
    public List<Command> getCommands() {
        return commands;
    }

}
