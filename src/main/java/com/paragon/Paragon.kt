package com.paragon

import com.paragon.api.event.EventFactory
import com.paragon.client.managers.*
import com.paragon.client.managers.social.SocialManager
import com.paragon.client.managers.alt.AltManager
import com.paragon.client.managers.notifications.NotificationManager
import com.paragon.client.managers.rotation.RotationManager
import com.paragon.client.ui.console.Console
import com.paragon.client.ui.taskbar.Taskbar
import com.paragon.client.ui.panel.PanelGUI
import com.paragon.client.ui.window.WindowGUI
import me.wolfsurge.cerauno.EventBus
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(name = Paragon.modName, modid = Paragon.modID, version = Paragon.modVersion)
class Paragon {

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent?) {
        logger = LogManager.getLogger("Paragon")

        fontManager = FontManager()
        eventParser = EventFactory()
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent?) {
        logger!!.info("Starting Paragon $modVersion initialisation")

        // Set up managers

        storageManager = StorageManager()
        logger!!.info("Storage Manager Initialised")

        popManager = PopManager()
        logger!!.info("Pop Manager Initialised")

        rotationManager = RotationManager()
        logger!!.info("Rotation Manager Initialised")

        socialManager = SocialManager()
        logger!!.info("Social Manager Initialised")

        altManager = AltManager()
        logger!!.info("Alt Manager Initialised")

        notificationManager = NotificationManager()
        logger!!.info("Notification Manager Initialised")

        capeManager = CapeManager()
        logger!!.info("Cape Manager Initialised")

        moduleManager = ModuleManager()
        logger!!.info("Module Manager Initialised")

        commandManager = CommandManager()
        logger!!.info("Command Manager Initialised")


        // GUIs

        taskbar = Taskbar()
        logger!!.info("Taskbar Initialised")

        panelGUI = PanelGUI()
        logger!!.info("Panel GUI Initialised")

        windowGUI = WindowGUI()
        logger!!.info("Window GUI Initialised")

        console = Console("Paragon Console", 400f, 300f)
        logger!!.info("Console Initialised")


        // Load config
        storageManager!!.loadModules("current")
        logger!!.info("Modules Loaded")

        storageManager!!.loadSocial()
        logger!!.info("Social Loaded")

        storageManager!!.loadAlts()
        logger!!.info("Alts Loaded")

        storageManager!!.loadOther()
        logger!!.info("Other Loaded")

        logger!!.info("Paragon $modVersion Initialised Successfully")
    }

    companion object {
        const val modName = "Paragon"
        const val modID = "paragon"
        const val modVersion = "1.0.0-DEV"

        @JvmField
        @Mod.Instance
        var INSTANCE = Paragon()
    }

    val eventBus = EventBus()

    // Client stuff
    var logger: Logger? = null
        private set

    val presenceManager = DiscordPresenceManager()

    // Managers
    var fontManager: FontManager? = null
        private set

    var storageManager: StorageManager? = null
        private set

    var moduleManager: ModuleManager? = null
        private set

    var commandManager: CommandManager? = null
        private set

    var eventParser: EventFactory? = null
        private set

    var popManager: PopManager? = null
        private set

    var rotationManager: RotationManager? = null
        private set

    var socialManager: SocialManager? = null
        private set

    var altManager: AltManager? = null
        private set

    var notificationManager: NotificationManager? = null
        private set

    var capeManager: CapeManager? = null
        private set

    // GUIs
    var taskbar: Taskbar? = null
        private set

    var panelGUI: PanelGUI? = null
        private set

    var windowGUI: WindowGUI? = null
        private set

    var console: Console? = null
        private set

    var isParagonMainMenu = false
        set(paragonMainMenu) {
            field = paragonMainMenu
            storageManager!!.saveOther()
        }
}