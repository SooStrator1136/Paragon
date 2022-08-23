package com.paragon.client.systems.command.impl

import com.paragon.Paragon
import com.paragon.client.systems.command.Command
import java.awt.Desktop
import java.io.File

object OpenFolderCommand : Command("OpenFolder", "openfolder") {

    override fun whenCalled(args: Array<String>, fromConsole: Boolean) {
        Desktop.getDesktop().open(File("paragon"))
        Paragon.INSTANCE.commandManager.sendClientMessage("Opened Paragon folder", false)
    }

}