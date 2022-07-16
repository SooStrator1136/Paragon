package com.paragon.client.managers

import club.minnced.discord.rpc.DiscordEventHandlers
import club.minnced.discord.rpc.DiscordRPC
import club.minnced.discord.rpc.DiscordRichPresence
import com.paragon.api.util.Wrapper

/**
 * @author Wolfsurge
 */
class DiscordPresenceManager : Wrapper {

    private val id = "965612502434082846"
    private val presence = DiscordRichPresence()
    private val rpc = DiscordRPC.INSTANCE

    fun startRPC() {
        val eventHandlers = DiscordEventHandlers()
        rpc.Discord_Initialize(id, eventHandlers, true, null)

        presence.largeImageKey = "logo"
        presence.startTimestamp = System.currentTimeMillis() / 1000L
        presence.largeImageKey = "Paragon"
        presence.largeImageText = "Paragon Client"
        presence.details = getDetails()
        presence.state = if (nullCheck()) "touching grass" else "beating newfags with paragon client"

        rpc.Discord_UpdatePresence(presence)
    }

    fun updateRPC() {
        presence.largeImageKey = "logo"
        presence.startTimestamp = System.currentTimeMillis() / 1000L
        presence.largeImageKey = "Paragon"
        presence.largeImageText = "Paragon Client"
        presence.details = getDetails()
        presence.state = if (nullCheck()) "touching grass" else if (minecraft.isSingleplayer) "trying out paragon in singleplayer" else "beating newfags with paragon client"
        rpc.Discord_UpdatePresence(presence)
    }

    fun stopRPC() {
        rpc.Discord_Shutdown()
        rpc.Discord_ClearPresence()
    }

    private fun getDetails() = if (nullCheck()) "Idling" else if (minecraft.isSingleplayer) "Singleplayer" else if (minecraft.currentServerData == null) "paragon client threw a java.lang.NullPointerException" else minecraft.currentServerData!!.serverIP

}