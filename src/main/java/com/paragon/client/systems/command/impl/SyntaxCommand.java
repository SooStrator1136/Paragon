package com.paragon.client.systems.command.impl;

import com.paragon.Paragon;
import com.paragon.client.managers.CommandManager;
import com.paragon.client.systems.command.Command;
import net.minecraft.util.text.TextFormatting;

public class SyntaxCommand extends Command {

    public SyntaxCommand() {
        super("Syntax", "syntax [command]");
    }

    @Override
    public void whenCalled(String[] args, boolean fromConsole) {
        if (args.length == 1) {
            for (Command command : Paragon.INSTANCE.getCommandManager().getCommands()) {
                if (command.getName().equalsIgnoreCase(args[0])) {
                    CommandManager.sendClientMessage(command.getSyntax(), fromConsole);
                    break;
                }
            }
        } else {
            CommandManager.sendClientMessage(TextFormatting.RED + "Invalid syntax!", fromConsole);
        }
    }

}
