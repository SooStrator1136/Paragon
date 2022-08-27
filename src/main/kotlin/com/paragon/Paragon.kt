package com.paragon

import com.paragon.api.event.EventFactory
import com.paragon.bus.EventBus
import com.paragon.client.managers.*
import com.paragon.client.managers.alt.AltManager
import com.paragon.client.managers.notifications.NotificationManager
import com.paragon.client.managers.rotation.RotationManager
import com.paragon.client.managers.social.SocialManager
import com.paragon.client.ui.configuration.ConfigurationGUI
import com.paragon.client.ui.configuration.retrowindows.Windows98
import com.paragon.client.ui.configuration.zeroday.ZerodayGUI
import com.paragon.client.ui.console.Console
import com.paragon.client.ui.taskbar.Taskbar
import net.minecraft.client.Minecraft
import net.minecraftforge.common.ForgeVersion
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.opengl.Display
import java.awt.Desktop
import java.net.URI
import javax.swing.JOptionPane

@Mod(name = Paragon.modName, modid = Paragon.modID, version = Paragon.modVersion)
class Paragon {

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent?) {
        if (ForgeVersion.buildVersion < 2860) {
            JOptionPane.showMessageDialog(null, "Forge version is too old. Paragon requires Forge to be at least build 2860.", "Outdated Forge!", JOptionPane.ERROR_MESSAGE)

            Desktop.getDesktop().browse(URI("https://files.minecraftforge.net/net/minecraftforge/forge/index_1.12.2.html"))

            Minecraft.getMinecraft().shutdown()

            // When trying to exit throws an exception lmao
            Display.destroy()

            return
        }

        eventParser = EventFactory()
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent?) {
        logger.info("Starting Paragon $modVersion initialisation")

        // Set up managers

        storageManager = StorageManager()
        logger.info("Storage Manager Initialised")

        popManager = PopManager()
        logger.info("Pop Manager Initialised")

        rotationManager = RotationManager()
        logger.info("Rotation Manager Initialised")

        socialManager = SocialManager()
        logger.info("Social Manager Initialised")

        altManager = AltManager()
        logger.info("Alt Manager Initialised")

        notificationManager = NotificationManager()
        logger.info("Notification Manager Initialised")

        capeManager = CapeManager()
        logger.info("Cape Manager Initialised")

        moduleManager = ModuleManager()
        logger.info("Module Manager Initialised")

        commandManager = CommandManager()
        logger.info("Command Manager Initialised")

        // Load config
        storageManager.loadModules("current")
        logger.info("Modules Loaded")

        storageManager.loadSocial()
        logger.info("Social Loaded")

        storageManager.loadAlts()
        logger.info("Alts Loaded")

        storageManager.loadOther()
        logger.info("Other Loaded")

        // GUIs

        taskbar = Taskbar
        logger.info("Taskbar Initialised")

        windows98GUI = Windows98()
        logger.info("Windows98 GUI Initialised")

        zerodayGUI = ZerodayGUI()
        logger.info("Panel GUI Initialised")

        console = Console("Paragon Console", 400f, 300f)
        logger.info("Console Initialised")

        configurationGUI = ConfigurationGUI()
        logger.info("Configuration GUI Initialised")

        logger.info("Paragon $modVersion Initialised Successfully")
    }

    companion object {
        const val modName = "Paragon"
        const val modID = "paragon"
        const val modVersion = "1.0.0 Pre 4"

        @JvmField
        @Mod.Instance
        var INSTANCE = Paragon()
    }

    val eventBus = EventBus()

    // Client stuff
    var logger: Logger = LogManager.getLogger("paragon")
        private set

    val presenceManager = DiscordPresenceManager()

    // Managers
    lateinit var storageManager: StorageManager
        private set

    lateinit var moduleManager: ModuleManager
        private set

    lateinit var commandManager: CommandManager
        private set

    lateinit var eventParser: EventFactory
        private set

    lateinit var popManager: PopManager
        private set

    lateinit var rotationManager: RotationManager
        private set

    lateinit var socialManager: SocialManager
        private set

    lateinit var altManager: AltManager
        private set

    lateinit var notificationManager: NotificationManager
        private set

    lateinit var capeManager: CapeManager
        private set

    // GUIs
    lateinit var taskbar: Taskbar
        private set

    lateinit var windows98GUI: Windows98
        private set

    lateinit var zerodayGUI: ZerodayGUI
        private set

    lateinit var configurationGUI: ConfigurationGUI
        private set

    lateinit var console: Console
        private set

    var isParagonMainMenu = false
        set(paragonMainMenu) {
            field = paragonMainMenu
            storageManager.saveOther()
        }

}