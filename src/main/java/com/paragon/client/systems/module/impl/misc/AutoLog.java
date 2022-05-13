package com.paragon.client.systems.module.impl.misc;

import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;
import net.minecraft.client.gui.GuiMainMenu;

/**
 * @author Wolfsurge
 */
public class AutoLog extends Module {

    private final Setting<DisconnectMode> logMode = new Setting<>("Log Mode", DisconnectMode.DISCONNECT)
            .setDescription("How to log you out of the server");

    private final Setting<Float> health = new Setting<>("Health", 6f, 1f, 20f, 1f)
            .setDescription("The health to log you out at");

    private final Setting<Boolean> autoDisable = new Setting<>("Auto Disable", true)
            .setDescription("Disables the module after logging you out");

    public AutoLog() {
        super("AutoLog", Category.MISC, "Automatically logs you out when you reach a certain health");
        this.addSettings(logMode, health, autoDisable);
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        if (mc.player.getHealth() <= health.getValue()) {
            switch (logMode.getValue()) {
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

            if (autoDisable.getValue()) {
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
