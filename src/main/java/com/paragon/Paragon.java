package com.paragon;

import com.paragon.client.managers.CommandManager;
import com.paragon.client.managers.ModuleManager;
import com.paragon.client.managers.StorageManager;
import com.paragon.client.managers.alt.AltManager;
import com.paragon.client.managers.social.SocialManager;
import com.paragon.client.systems.ui.console.Console;
import com.paragon.client.systems.ui.panel.PanelGUI;
import com.paragon.client.systems.ui.taskbar.Taskbar;
import com.paragon.client.systems.ui.window.WindowGUI;
import me.wolfsurge.cerauno.EventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(name = Paragon.modName, modid = Paragon.modID, version = Paragon.modVersion)
public class Paragon {

    public static final String modName = "Paragon";
    public static final String modID = "paragon";
    public static final String modVersion = "1.0.0";

    @Mod.Instance
    public static Paragon INSTANCE = new Paragon();

    // Client stuff
    private Logger logger;
    private final EventBus eventBus = new EventBus();

    // Managers
    private StorageManager storageManager;
    private ModuleManager moduleManager;
    private CommandManager commandManager;
    private SocialManager socialManager;
    private AltManager altManager;

    // GUIs
    private Taskbar taskbar;
    private PanelGUI panelGUI;
    private WindowGUI windowGUI;
    private Console console;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = LogManager.getLogger("Paragon");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        getLogger().info("Starting Paragon " + modVersion + " initialisation");

        // Set up managers
        storageManager = new StorageManager();
        moduleManager = new ModuleManager();
        commandManager = new CommandManager();
        socialManager = new SocialManager();
        altManager = new AltManager();

        // Set up GUIs and elements
        taskbar = new Taskbar();
        panelGUI = new PanelGUI();
        windowGUI = new WindowGUI();
        console = new Console("Paragon Console", 400, 300);

        getLogger().info("Paragon Initialised Successfully");
    }

    /**
     * Gets the logger
     * @return The logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Gets the event bus
     * @return The event bus
     */
    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * Gets the storage manager
     * @return The storage manager
     */
    public StorageManager getStorageManager() {
        return storageManager;
    }

    /**
     * Gets the module manager
     * @return The client's module manager
     */
    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    /**
     * Gets the command manager
     * @return The command manager
     */
    public CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * Gets the social manager
     * @return The social manager
     */
    public SocialManager getSocialManager() {
        return socialManager;
    }

    /**
     * Gets the alt manager
     * @return The alt manager
     */
    public AltManager getAltManager() {
        return altManager;
    }

    /**
     * Gets the taskbar
     * @return The taskbar
     */
    public Taskbar getTaskbar() {
        return taskbar;
    }

    /**
     * Gets the panel GUI
     * @return The panel GUI
     */
    public PanelGUI getPanelGUI() {
        return panelGUI;
    }

    /**
     * Gets the window GUI
     * @return The window GUI
     */
    public WindowGUI getWindowGUI() {
        return windowGUI;
    }

    /**
     * Gets the console
     * @return The console
     */
    public Console getConsole() {
        return console;
    }
}
