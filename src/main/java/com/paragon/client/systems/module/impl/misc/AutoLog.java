package com.paragon.client.systems.module.impl.misc;

import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import net.minecraft.client.gui.GuiMainMenu;

/**
 * @author Wolfsurge
 */
public class AutoLog extends Module {

    private final ModeSetting<DisconnectMode> logMode = new ModeSetting<>("Log Mode", "How to log you out of the server", DisconnectMode.DISCONNECT);
    private final NumberSetting health = new NumberSetting("Health", "The health to log you out at", 6, 1, 20, 1);
    private final BooleanSetting autoDisable = new BooleanSetting("Auto Disable", "Disables the module after logging you out", true);

    public AutoLog() {
        super("AutoLog", ModuleCategory.MISC, "Automatically logs you out when you reach a certain health");
        this.addSettings(logMode, health, autoDisable);
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        if (mc.player.getHealth() <= health.getValue()) {
            switch (logMode.getCurrentMode()) {
                case KICK:
                    // Set current item to an invalid number
                    mc.player.inventory.currentItem = -1;
                    break;
                case DISCONNECT:
                    // Disconnect from server
                    mc.world.sendQuittingDisconnectingPacket();
                    mc.loadWorld(null);
                    mc.displayGuiScreen(new GuiMainMenu());
                    break;
            }

            if (autoDisable.isEnabled()) {
                // Toggle module state
                toggle();
            }
        }
    }

    public enum DisconnectMode {
        /**
         * Disconnects you from the server
         */
        DISCONNECT,

        /**
         * Kicks you from the server by setting your current item to an invalid slot
         */
        KICK
    }
}
