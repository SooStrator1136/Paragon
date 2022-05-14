package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.network.PlayerEvent;
import com.paragon.api.util.calculations.Timer;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Wolfsurge
 */
public class Announcer extends Module {

    // Event settings
    private final Setting<Double> chatTimer = new Setting<>("Chat Timer", 5D, 1D, 60D, 1D)
            .setDescription("The amount of time in seconds between each chat message");

    private final Setting<Boolean> breakBlocks = new Setting<>("Break Blocks", true)
            .setDescription("Announce when a block is broken");

    private final Setting<Boolean> playerJoin = new Setting<>("Player Join", true)
            .setDescription("Announce when players join the server");

    private final Setting<Boolean> playerLeave = new Setting<>("Player Leave", true)
            .setDescription("Announce when players leave the server");

    // Timer to determine when we should send the message
    private final Timer timer = new Timer();

    // Part 1 is the first part of the message, Part 2 is the value, Part 3 is the second part of the message
    private String[] announceComponents = new String[]{"", "0", ""};

    public Announcer() {
        super("Announcer", Category.MISC, "Announces events to the chat");
        this.addSettings(chatTimer, breakBlocks, playerJoin, playerLeave);
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        if (timer.hasMSPassed(chatTimer.getValue() * 1000) && !announceComponents[0].equals("") && !announceComponents[2].equals("")) {
            mc.player.sendChatMessage(announceComponents[0] + announceComponents[1] + announceComponents[2]);
            announceComponents = new String[]{"", "0", ""};

            timer.reset();
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (breakBlocks.getValue()) {
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
        if (playerJoin.getValue()) {
            if (!event.getName().equals(mc.player.getName())) {
                mc.player.sendChatMessage("Welcome to the server, " + event.getName() + "!");
            }
        }
    }

    @Listener
    public void onPlayerLeave(PlayerEvent.PlayerLeaveEvent event) {
        if (playerLeave.getValue()) {
            if (!event.getName().equals(mc.player.getName())) {
                mc.player.sendChatMessage("See you later, " + event.getName() + "!");
            }
        }
    }
}
