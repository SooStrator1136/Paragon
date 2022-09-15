package com.paragon.impl.command.impl

import com.paragon.Paragon
import com.paragon.impl.managers.notifications.Notification
import com.paragon.impl.managers.notifications.NotificationType
import java.io.File

/**
 * @author Surge
 */
object ConfigCommand : com.paragon.impl.command.Command("Config", "config [load/save] [name]") {

    override fun whenCalled(args: Array<String>, fromConsole: Boolean) {
        if (args.size == 2) {
            if (args[0].equals("save", ignoreCase = true)) {
                Paragon.INSTANCE.storageManager.saveModules(args[1])
                Paragon.INSTANCE.notificationManager.addNotification(
                    Notification(
                        "Saved config " + args[1], NotificationType.INFO
                    )
                )
            }
            else if (args[0].equals("load", ignoreCase = true)) {
                Paragon.INSTANCE.storageManager.loadModules(args[1])
                Paragon.INSTANCE.notificationManager.addNotification(
                    Notification(
                        "Loading config " + args[1], NotificationType.INFO
                    )
                )
            }
            else if (args[0].equals("delete", ignoreCase = true)) {
                val file = File("paragon" + File.separator + "configs" + File.separator + args[1])
                if (file.exists()) {
                    file.delete()
                }
                else {
                    Paragon.INSTANCE.notificationManager.addNotification(
                        Notification(
                            "Config " + args[1] + " does not exist", NotificationType.ERROR
                        )
                    )
                }
            }
        }
        else {
            Paragon.INSTANCE.notificationManager.addNotification(
                Notification(
                    "Syntax: $syntax", NotificationType.ERROR
                )
            )
        }
    }

}