package com.paragon.impl.command.impl

import com.paragon.Paragon
import java.awt.Desktop
import java.io.File

object OpenFolderCommand : com.paragon.impl.command.Command("OpenFolder", "openfolder") {

    override fun whenCalled(args: Array<String>, fromConsole: Boolean) {
        Desktop.getDesktop().open(File("paragon"))
        Paragon.INSTANCE.commandManager.sendClientMessage("Opened Paragon folder", fromConsole)
    }

}