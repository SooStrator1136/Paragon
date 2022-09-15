package com.paragon.impl.managers

import club.minnced.discord.rpc.DiscordEventHandlers
import club.minnced.discord.rpc.DiscordRPC
import club.minnced.discord.rpc.DiscordRichPresence
import com.paragon.util.Wrapper
import com.paragon.util.anyNull
import com.paragon.util.mc

/**
 * @author Surge
 */
class DiscordPresenceManager : Wrapper {

    private val id = "965612502434082846"
    private val presence = DiscordRichPresence()
    private val rpc = DiscordRPC.INSTANCE

    fun startRPC() {
        val eventHandlers = DiscordEventHandlers()
        rpc.Discord_Initialize(id, eventHandlers, true, null)

        presence.largeImageKey = "logo"
        presence.largeImageText = "Paragon Client @ https://github.com/Wolfsurge/Paragon"
        presence.startTimestamp = System.currentTimeMillis() / 1000L
        presence.details = getDetails()
        presence.state = "Username: " + mc.session.username
        rpc.Discord_UpdatePresence(presence)
    }

    fun updateRPC() {
        presence.largeImageKey = "logo"
        presence.largeImageText = "Paragon Client @ https://github.com/Wolfsurge/Paragon"
        presence.details = getDetails()
        presence.state = "Username: " + mc.session.username
        rpc.Discord_UpdatePresence(presence)
    }

    fun stopRPC() {
        rpc.Discord_Shutdown()
        rpc.Discord_ClearPresence()
    }

    private fun getDetails(): String {
        return if (minecraft.anyNull || !com.paragon.impl.module.client.DiscordRPC.showServer.value) {
            "Idling"
        }

        else if (minecraft.isSingleplayer) {
            "Playing on singleplayer"
        }

        else if (minecraft.currentServerData == null) {
            "paragon client threw a java.lang.NullPointerException"
        }
        else "Playing on " + minecraft.currentServerData!!.serverIP
    }

}
