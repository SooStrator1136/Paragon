package com.paragon.client.managers;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import com.paragon.Paragon;

public class DiscordPresenceManager {

    private final String id = "965612502434082846";
    private final DiscordRichPresence presence = new DiscordRichPresence();
    private final DiscordRPC rpc = DiscordRPC.INSTANCE;

    public void startRPC() {
        DiscordEventHandlers eventHandlers = new DiscordEventHandlers();
        rpc.Discord_Initialize(id, eventHandlers, true, null);

        presence.largeImageKey = "logo";
        presence.startTimestamp = System.currentTimeMillis() / 1000L;
        presence.details = Paragon.modVersion;
        presence.largeImageKey = "Paragon";
        presence.largeImageText = "Paragon Client";
        presence.state = null;
        rpc.Discord_UpdatePresence(presence);
    }

    public void stopRPC() {
        rpc.Discord_Shutdown();
        rpc.Discord_ClearPresence();
    }

}
