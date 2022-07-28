package com.paragon.client.systems.command.impl;

import com.paragon.Paragon;
import com.paragon.client.managers.notifications.Notification;
import com.paragon.client.managers.notifications.NotificationType;
import com.paragon.client.systems.command.Command;

import java.io.File;

public class ConfigCommand extends Command {

    public ConfigCommand() {
        super("Config", "config [load/save] [name]");
    }

    @Override
    public void whenCalled(String[] args, boolean fromConsole) {
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("save")) {
                Paragon.INSTANCE.getStorageManager().saveModules(args[1]);
                Paragon.INSTANCE.getNotificationManager().addNotification(new Notification("Saved config " + args[1], NotificationType.INFO));
            }

            if (args[0].equalsIgnoreCase("load")) {
                Paragon.INSTANCE.getStorageManager().loadModules(args[1]);
                Paragon.INSTANCE.getNotificationManager().addNotification(new Notification("Loading config " + args[1], NotificationType.INFO));
            }

            if (args[0].equalsIgnoreCase("delete")) {
                File file = new File("paragon" + File.separator + "configs" + File.separator + args[1]);

                if (file.exists()) {
                    file.delete();
                }

                else {
                    Paragon.INSTANCE.getNotificationManager().addNotification(new Notification("Config " + args[1] + " does not exist", NotificationType.ERROR));
                }
            }
        } else {
            Paragon.INSTANCE.getNotificationManager().addNotification(new Notification("Syntax: " + getSyntax(), NotificationType.ERROR));
        }
    }
}
