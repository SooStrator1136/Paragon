package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.network.PlayerEvent;
import com.paragon.api.util.calculations.Timer;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Wolfsurge
 */
public class Announcer extends Module {

    // Event settings
    private final NumberSetting chatTimer = new NumberSetting("Chat Timer", "The amount of time in seconds between each chat message", 5, 1, 60, 1);
    private final BooleanSetting breakBlocks = new BooleanSetting("Break Blocks", "Announce when blocks are broken", true);
    private final BooleanSetting playerJoin = new BooleanSetting("Player Join", "Announce when players join the server", true);
    private final BooleanSetting playerLeave = new BooleanSetting("Player Leave", "Announce when players leave the server", true);

    // Timer to determine when we should send the message
    private final Timer timer = new Timer();

    // Part 1 is the first part of the message, Part 2 is the value, Part 3 is the second part of the message
    private String[] announceComponents = new String[] { "", "0", ""};

    public Announcer() {
        super("Announcer", ModuleCategory.MISC, "Announces events to the chat");
        this.addSettings(chatTimer, breakBlocks, playerJoin, playerLeave);
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        if (timer.hasMSPassed((long) chatTimer.getValue() * 1000) && !announceComponents[0].equals("") && !announceComponents[2].equals("")) {
            mc.player.sendChatMessage(announceComponents[0] + announceComponents[1] + announceComponents[2]);
            announceComponents = new String[] { "", "0", ""};

            timer.reset();
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (breakBlocks.isEnabled()) {
            String first = "I just broke ";
            String third = " blocks!";

            if (!announceComponents[0].equals(first) && !announceComponents[2].equals(third)) {
                announceComponents = new String[]{first, "0", third};
            }

            announceComponents = new String[]{first, String.valueOf(Integer.parseInt(announceComponents[1]) + 1), third};
        }
    }

    @Listener
    public void onPlayerJoin(PlayerEvent.PlayerJoinEvent event) {
        if (playerJoin.isEnabled()) {
            if (!event.getName().equals(mc.player.getName())) {
                mc.player.sendChatMessage("Welcome to the server, " + event.getName() + "!");
            }
        }
    }

    @Listener
    public void onPlayerLeave(PlayerEvent.PlayerLeaveEvent event) {
        if (playerLeave.isEnabled()) {
            if (!event.getName().equals(mc.player.getName())) {
                mc.player.sendChatMessage("See you later, " + event.getName() + "!");
            }
        }
    }
}
