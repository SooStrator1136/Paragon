package com.paragon.client.systems.command.impl;

import com.paragon.Paragon;
import com.paragon.client.managers.CommandManager;
import com.paragon.client.systems.command.Command;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("Help", "help");
    }

    @Override
    public void whenCalled(String[] args, boolean fromConsole) {
        for (Command command : Paragon.INSTANCE.getCommandManager().getCommands()) {
            Paragon.INSTANCE.getCommandManager().sendClientMessage(command.getName(), fromConsole);
        }
    }
}
