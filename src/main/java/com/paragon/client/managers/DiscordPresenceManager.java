package com.paragon.client.managers;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import com.paragon.Paragon;
import com.paragon.api.util.Wrapper;

public class DiscordPresenceManager implements Wrapper {

    private final String id = "965612502434082846";
    private final DiscordRichPresence presence = new DiscordRichPresence();
    private final DiscordRPC rpc = DiscordRPC.INSTANCE;

    public void startRPC() {
        DiscordEventHandlers eventHandlers = new DiscordEventHandlers();
        rpc.Discord_Initialize(id, eventHandlers, true, null);

        presence.largeImageKey = "logo";
        presence.startTimestamp = System.currentTimeMillis() / 1000L;
        presence.largeImageKey = "Paragon";
        presence.largeImageText = "Paragon Client";
        presence.details = nullCheck() ? "Idling" : mc.isSingleplayer() ? "Singleplayer" : mc.getCurrentServerData() == null ? "pargon client threw a java.lang.NullPointerException" : mc.getCurrentServerData().serverIP;
        presence.state = nullCheck() ? "touching grass" : "beating newfags with paragon client";

        rpc.Discord_UpdatePresence(presence);
    }

    public void updateRPC() {
        presence.largeImageKey = "logo";
        presence.startTimestamp = System.currentTimeMillis() / 1000L;
        presence.largeImageKey = "Paragon";
        presence.largeImageText = "Paragon Client";
        presence.details = nullCheck() ? "Idling" : mc.isSingleplayer() ? "Singleplayer" : mc.getCurrentServerData() == null ? "pargon client threw a java.lang.NullPointerException" : mc.getCurrentServerData().serverIP;
        presence.state = nullCheck() ? "touching grass" : mc.isSingleplayer() ? "trying out paragon in singleplayer" : "beating newfags with paragon client";

        rpc.Discord_UpdatePresence(presence);
    }

    public void stopRPC() {
        rpc.Discord_Shutdown();
        rpc.Discord_ClearPresence();
    }

}
